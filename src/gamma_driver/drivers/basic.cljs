(ns gamma-driver.drivers.basic
  (:require
    [gamma-driver.api :as gd]
    [gamma-driver.protocols :as gdp]))


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
         val (constructor-fn (gd/gl driver) spec)]
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




;; want to dispatch to a particular bind* method
;; each one will handle a case like attributes versus textures etc
(defn program-inputs-state [driver program]
  (let [s (@(:input-state driver) program)]
    (into {}
          (map #(vector % (s %)) (:inputs program)))))

(defn input-complete? [driver program]
  (let [state   (@(:input-state driver) program)
        inputs  (:inputs program)]
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

(defn draw-arrays*
  ([driver program opts]
    (draw-arrays* driver program opts nil))
  ([driver program opts target]
   (if (not (input-complete? driver program))
     (throw (js/Error. "Program inputs are incomplete."))
     (gd/draw-arrays
       (gdp/gl driver)
       program
       ;; should supply below as an arg, with defaults
       {:draw-mode (:draw-mode opts :triangles)
        :first 0
        :count (draw-count driver program)}
       target))))


(defn draw-elements*
  ([driver program opts]
    (draw-elements* driver program opts nil))
  ([driver program opts target]
    (if (not (input-complete? driver program))
     (throw (js/Error. "Program inputs are incomplete."))
     (gd/draw-elements
      (gdp/gl driver)
       program
       ;; should supply below as an arg, with defaults
       {:draw-mode  (:draw-mode opts :triangles)
        :first      0
        ;; Should we just throw an error if :index-type isn't specified,
        ;; rather than default to :unsigned-short?  Seems kinder.
        :index-type :unsigned-short
        :count      (:count opts)}
       target))))




(defrecord BasicDriver [gl resource-state mapping-fn input-state input-fn]
  gdp/IContext
  (configure [this spec] (gd/configure gl spec))
  (gl [this] gl)

  gdp/IResource
  (program [this spec]   (gd/program gl spec))
  (array-buffer [this spec] (produce this gd/array-buffer spec))
  (element-array-buffer [this spec] (produce this gd/element-array-buffer spec))
  (texture [this spec] (produce this gd/texture spec))
  (frame-buffer [this spec] (produce this gd/frame-buffer spec))
  (render-buffer [this spec] (produce this gd/render-buffer spec))
  (release [this spec] (let [k (mapping-fn spec)]
                         (gd/release gl spec)
                         (swap! resource-state dissoc k)))

  gdp/IBindVariable
  (bind-attribute [this program attribute input]
    (input-fn
      this
      program
      gd/bind-attribute
      attribute
      input))
  (bind-texture-uniform [this program uniform input]
    (input-fn
      this
      program
      gd/bind-texture-uniform
      uniform
      input))
  (bind-uniform [this program uniform input]
    (input-fn
      this
      program
      gd/bind-uniform
      uniform
      input))

  gdp/IDraw
  (draw-arrays [this program spec] (draw-arrays* this program spec))
  (draw-arrays [this program spec target] (draw-arrays* this program spec target))
  (draw-elements [this program spec] (draw-elements* this program spec))
  (draw-elements [this program spec target] (draw-elements* this program spec target)))


(defn basic-driver [gl]
  (BasicDriver.
    gl
    (atom {})
    (fn [x] (or (:id x) (:element x) x))
    (atom {})
    default-input-fn))







(comment
  (bind-dispatch-fn {:tag :variable :storage :attribute} nil)
  (bind-dispatch-fn {:tag :variable :storage :uniform} nil)
  (bind-dispatch-fn {:tag :variable :storage :uniform :type :sampler2D} nil)
  (bind-dispatch-fn {:tag :element-index} nil)
  (bind-dispatch-fn {:tag :variable-array} nil))





