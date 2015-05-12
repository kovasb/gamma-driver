(ns gamma-driver.drivers.basic
  (:require [gamma-driver.common.resource :as r]
            [gamma-driver.common.variable :as v]
            [gamma-driver.common.configure :as c]
            [gamma-driver.common.draw :as d]
            [gamma-driver.protocols :as proto]))


(defn produce [driver constructor-fn spec]
  (let [{:keys [gl resource-state mapping-fn]} driver]
    (let [k (mapping-fn spec)
         spec (if-let [x (@resource-state k)] (merge x spec) spec)
         val (constructor-fn (proto/gl driver) spec)]
     (swap! resource-state assoc k val)
     val)))


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
    (fn [x] (or (:id x) (:variables x) x))
    (atom {})
    default-input-fn))



(defn variable-input [driver program variable input]
  (cond
    (= :attribute (:storage variable))
    (proto/attribute-input
      driver
      program
      variable
      (proto/array-buffer driver (assoc input :usage :static-draw)))

    (and (= :uniform (:storage variable)) (= :sampler2D (:type variable)))
    (proto/texture-uniform-input
      driver
      program
      variable
      (proto/texture driver (if (map? (:data input))
                              (:data input)
                              {:image (:data input) :texture-id 0})))

    (= :uniform (:storage variable))
    (proto/uniform-input driver program variable (:data input))))


(defn bind [driver program data]
  (.useProgram (:gl driver) (:program program))
  (doall
    (flatten
     (for [d data]
       (for [v (:variables d)]
         (variable-input driver program v d))))))


(defn normalize-data [data]
  (map (fn [[k v]]
         (if (= :attribute (:storage k))
           {:variables [k]
            :data      (js/Float32Array. (clj->js (flatten v)))
            :count     (count v)}
           {:variables [k] :data v}))
       data))

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



(defn draw-program [driver program data]
  (bind driver program data)
  (if (not (input-complete? driver program))
    (throw (js/Error. "Program inputs are incomplete."))
    (proto/draw-arrays
      driver
      program
      ;; should supply below as an arg, with defaults
      {:draw-mode :triangles
       :first 0
       :count (draw-count driver program)})))








