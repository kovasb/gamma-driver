(ns gamma.webgl.shader
  (:require [gamma.webgl.api :as api]
            [goog.webgl :as ggl]
    ;[gamma-driver.impl.resource :as resource]
            [gamma.webgl.uniform :as uniform]
            [gamma.webgl.attribute :as attribute]))

(defn install-shader* [gl spec]
  (let [s (.createShader
            gl
            ({:vertex-shader ggl/VERTEX_SHADER
              :fragment-shader ggl/FRAGMENT_SHADER}
              (:tag spec)))]
    (if s
      (do
        (.shaderSource gl s (:glsl spec))
        (.compileShader gl s)
        (let [compiled (.getShaderParameter gl s ggl/COMPILE_STATUS)]
          (if compiled
            (assoc spec (:tag spec) s)
            (throw (js/Error. (str "failed to compile " (name (:tag spec)) ":"
                                   (.getShaderInfoLog gl s)))))))
      (throw (js/Error. (str "Unable to create " (name (:tag spec))))))))


(defn install-shader [gl spec]
  (let [v (install-shader* gl (assoc (:vertex-shader spec) :tag :vertex-shader))
        f (install-shader* gl (assoc (:fragment-shader spec) :tag :fragment-shader))
        p (.createProgram gl)]
    (.attachShader gl p (:vertex-shader v))
    (.attachShader gl p (:fragment-shader f))
    (.linkProgram gl p)
    (if (.getProgramParameter gl p ggl/LINK_STATUS)
      (do
        (.useProgram gl p)
        (assoc spec :program p
                    :vertex-shader v
                    :fragment-shader f))
      (throw
        (js/Error.
          (str "failed to link program: "
               (.getProgramInfoLog gl p)))))))



(defn variable->input [context shader v]
  (cond
    (= :attribute (:storage v))
    (attribute/attribute context shader v)
    (= :uniform (:storage v))
    (uniform/uniform context shader v)))


(defrecord Shader [context program inputs]
  api/IProgram
  (program [this] program)
  (inputs [this]
    (reduce
      (fn [init v]
        (assoc init v (variable->input context this v)))
      {}
      inputs)))

(defn shader [context program-source]
  (let [p (install-shader (api/gl context) program-source)]
    (Shader. context (:program p) (:inputs p))))


(defrecord CurrentShader [context]
  api/IInput
  (input! [this data] (.useProgram (api/gl context) (api/program data))))

(defn current-shader [context] (CurrentShader. context))
