(ns gamma-driver.drivers.basic
  (:require [gamma-driver.common.resource :as r]
            [gamma-driver.common.variable :as v]
            [gamma-driver.common.configure :as c]
            [gamma-driver.common.draw :as d]
            [gamma-driver.protocols :as proto]))


;; WebGLResourceDriver implementations wrap the low-level constructors fns
;; found in common.resource.cljs with produce
;; this lets us reuse prexisting array buffers

(defn produce [driver constructor-fn spec]
  (let [{:keys [gl resource-state mapping-fn]} driver]
    (let [
          ;; given input, apply mapping-fn to get its key in the resource-state map
          k (mapping-fn spec)
          ;; if already there, merge spec in
         spec (if-let [x (@resource-state k)] (merge x spec) spec)
          ;;  call constructor fn with the merged map
         val (constructor-fn (proto/gl driver) spec)]
     (swap! resource-state assoc k val)
     val)))


;; this part wraps resource binding, eg connecting buffers to attributes
;; input-binder is the fn from variable.cljs
;; this keeps track of what resource was bound to what [program variable] pair
;; -> allows us to see if we've inputted data for all variables, instead of getting
;;    just a black screen
;; -> allows us to compute how many vertices to draw, rather than calculating by hand
;; keeps track of this on the :input-state atom on driver
(defn default-input-fn [driver program input-binder variable spec]
  (let [{:keys [input-state gl]} driver
        i (input-binder gl program variable spec)]
    (swap! input-state assoc-in [program variable] spec)
    i))


(defrecord BasicDriver [gl resource-state mapping-fn input-state input-fn]
  proto/WebGLContextDriver
  (configure [this spec] (c/configure gl spec))
  (gl [this] gl)

  proto/WebGLResourceDriver
  (program [this spec]   (r/program gl spec))
  (array-buffer [this spec] (produce this r/array-buffer spec))
  (element-array-buffer [this spec] (produce this r/element-array-buffer spec))
  (texture [this spec] (produce this r/texture spec))
  (frame-buffer [this spec] (produce this r/frame-buffer spec))
  (render-buffer [this spec] (produce this r/render-buffer spec))
  (release [this spec] (let [k (mapping-fn spec)]
                         (r/release gl spec)
                         (swap! resource-state dissoc k)))

  proto/WebGLVariableDriver
  (attribute-input [this program attribute input]
    (input-fn
      this
      program
      v/attribute-input
      attribute
      input))
  (element-index-input [this program attribute input]
    (input-fn
      this
      program
      v/element-index-input
      attribute
      input))
  (texture-uniform-input [this program uniform input]
    (input-fn
      this
      program
      v/texture-uniform-input
      uniform
      input))
  (uniform-input [this program uniform input]
    (input-fn
      this
      program
      v/uniform-input
      uniform
      input))

  proto/WebGLDrawDriver
  (draw-arrays [this program spec] (d/draw-arrays gl program spec))
  (draw-arrays [this program spec target] (d/draw-arrays gl program spec target))
  (draw-elements [this program spec] (d/draw-elements gl program spec))
  (draw-elements [this program spec target] (d/draw-elements gl program spec target)))


(defn basic-driver [gl]
  (BasicDriver.
    gl
    (atom {})
    (fn [x] (or (:id x) (:element x) x))
    (atom {})
    default-input-fn))


;; want to dispatch to a particular bind* method
;; each one will handle a case like attributes versus textures etc
(defn bind-dispatch-fn [element data]
  (if (= :variable (:tag element))
    (cond
      (= :attribute (:storage element)) :attribute
      (and
        (= :uniform (:storage element))
        (= :sampler2D (:type element))) :texture-uniform
      (= :uniform (:storage element)) :uniform)
    (cond
      (= :element-index (:tag element)) :element-index
      (= :variable-array (:tag element)) :variable-array)))


(defmulti bind*
          (fn [driver program element data]
            (bind-dispatch-fn element data)))


(defmethod bind* :attribute [driver program element input]
  (proto/attribute-input
    driver
    program
    element
    (proto/array-buffer
      driver
      (let [input (if (map? input) input {:data input})]
        (assoc input
          :data (js/Float32Array. (clj->js (flatten (:data input))))
          :usage :static-draw
          :element element
          :count (count (:data input)))))))

(defmethod bind* :uniform [driver program element input]
  (proto/uniform-input
    driver
    program
    element
    (let [input (if (map? input) input {:data input})]
      (assoc input
        :element element
        :data (clj->js (flatten [(:data input)]))))))

(defmethod bind* :element-index [driver program element input]
  (proto/element-index-input
    driver
    program
    element
    (proto/element-array-buffer
      driver
      (let [input (if (map? input) input {:data input})]
        (assoc input
          ;; Probably already flattened, but keeping it here for now
          :data (js/Uint16Array. (clj->js (flatten (:data input))))
          :usage :static-draw
          :element element
          :test :work
          :count (count (:data input)))))))


(comment
  (defmethod bind* :texture-uniform [driver program variable input]
   (proto/texture-uniform-input
     driver
     program
     variable
     (proto/texture
       driver
       ;; not sure if this is the right logic
       (if (map? (:data input))
         (:data input)
         {:image (:data input) :texture-id 0})))))

(comment
  (defmethod bind* :element-array [driver program variable input]
   (proto/element-array-buffer driver (assoc input :usage :static-draw))))


(defn bind [driver program data]
  (.useProgram (:gl driver) (:program program))
  (doseq [[k v] data]
    (bind* driver program k v)))

(defn program-inputs-state [driver program]
  (let [s (@(:input-state driver) program)]
    (into {}
          (map #(vector % (s %)) (:inputs program)))))

(defn input-complete? [driver program]
  (let [state (@(:input-state driver) program)
        inputs (:inputs program)]
    (not-any? nil? (map state inputs))))
;; would like return value to indicate which inputs are not filled in?


(defn draw-count [driver program]
  ;; for now just pick the first attribute that has a count
  (first
    (keep
      (fn [[k v]]
        (if (= :attribute (:storage k))
          (:count v)))
      (@(:input-state driver) program))))

(defn draw-program [driver program data & [opts]]
  (bind driver program data)
  (if (not (input-complete? driver program))
    (throw (js/Error. "Program inputs are incomplete."))
    (proto/draw-arrays
      driver
      program
      ;; should supply below as an arg, with defaults
      {:draw-mode (:draw-mode opts :triangles)
       :first 0
       :count (draw-count driver program)})))

(defn draw-elements [driver program data opts]
  (bind driver program data)
  (if (not (input-complete? driver program))
    (throw (js/Error. "Program inputs are incomplete."))
    (proto/draw-elements
     driver
     program
     ;; should supply below as an arg, with defaults
     {:draw-mode (:draw-mode opts :triangles)
      :first 0
      ;; Should we just throw an error if :index-type isn't specified,
      ;; rather than default to :unsigned-short?  Seems kinder.
      :index-type (:draw-type opts :unsigned-short)
      :count (:count opts)})))





(comment
  (bind-dispatch-fn {:tag :variable :storage :attribute} nil)
  (bind-dispatch-fn {:tag :variable :storage :uniform} nil)
  (bind-dispatch-fn {:tag :variable :storage :uniform :type :sampler2D} nil)
  (bind-dispatch-fn {:tag :element-index} nil)
  (bind-dispatch-fn {:tag :variable-array} nil))





