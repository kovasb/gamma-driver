(ns gamma.webgl.gradient
  (:require
    [gamma.api :as g]
    [gamma.webgl.api :as gd]
    [gamma.webgl.shader :as shader]
    [gamma.webgl.routines.basic :as r]
    [gamma.webgl.drivers.basic :as driver]
    [gamma.webgl.compiler.core]))


(def pos-attribute (g/attribute "posAttr" :vec2))

(def color-attribute (g/attribute "colorAttr" :vec4))

(def color-varying (g/varying "colorVarying" :vec4 :mediump))

(defn example-shader []
  (shader/compile
    {:id              :gradient
     :vertex-shader   {(g/gl-position) (g/vec4 pos-attribute 0 1)
                       color-varying color-attribute}
     :fragment-shader {(g/gl-frag-color) color-varying} }))

(defn ->float32 [x]
  (js/Float32Array.
    (clj->js (flatten x))))


(defn example-data []
  {:shader {pos-attribute   (->float32 [[-0.5 -0.5] [0.5 -0.5] [0 0]])
            color-attribute (->float32 [[1 0 0 1] [0 1 0 1] [0 0 1 1]])}
   :draw   {:start 0 :count 3}})

(defn get-context [id]
  (.getContext
    (.getElementById js/document id)
    "webgl"))



(defn main []
  (let [r (r/shader-draw (example-shader))
       driver (driver/driver
                (:commands r)
                {:gl (get-context "gl-canvas")})]
   (driver/exec!
     driver
     (driver/assoc-inputs
       (:inputs r)
       (example-data)))))




