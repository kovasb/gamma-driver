(ns gamma.webgl.model.uniforms
  (:require [gamma.webgl.model.core :as m]))

(defn bind-uniform-fn [type gl location]
  ;(println [type location data])
  (case type
    :bool #(.uniform1iv gl location %)
    :bvec2 #(.uniform2iv gl location %)
    :bvec3 #(.uniform3iv gl location %)
    :bvec4 #(.uniform4iv gl location %)
    :float #(.uniform1fv gl location %)
    :vec2 #(.uniform2fv gl location %)
    :vec3 #(.uniform3fv gl location %)
    :vec4 #(.uniform4fv gl location %)
    :int #(.uniform1iv gl location %)
    :ivec2 #(.uniform2iv gl location %)
    :ivec3 #(.uniform3iv gl location %)
    :ivec4 #(.uniform4iv gl location %)
    :mat2 #(.uniformMatrix2fv gl location false %)
    :mat3 #(.uniformMatrix3fv gl location false %)
    :mat4 #(.uniformMatrix4fv gl location false %)
    :sampler2D #(.uniform1i gl location %)
    nil))

(defrecord TextureUniform [root uniform-fn]
  m/IModel
  (conform [this val]
    ;; get rid of allocation here? first set tex unit, then texture
    ;; would involve texture-data as well
    (m/conform root {:texture-units {(:texture-unit val) (:texture val)}})
    (uniform-fn (:texture-unit val)))
  (resolve [this val]))



(defrecord Uniform [root uniform-fn]
  m/IModel
  (conform [this val]
    (uniform-fn val))
  (resolve [this val]))


(comment
  (defn bind-uniforms [root val locations uniform-data-fn]
   (reduce-kv
     (fn [_ k v]
       (bind-uniform
         (:type k)
         (:gl root)
         (locations k)
         (uniform-data-fn v)))
     nil
     val)))

(defrecord Uniforms [root parts]
  m/IModel
  (resolve [this val]
    (@parts val))
  (conform [this val]
    (m/delegate m/conform #(m/resolve this %) val)))

(defn uniforms [root variable-locations]
  (let [gl (:gl root)
        us (reduce-kv
             (fn [x k v]
               (assoc
                 x
                 k
                 (let [bf (bind-uniform-fn (:type k) gl v)]
                   (if (= :sampler2D (:type k))
                     (->TextureUniform root bf)
                     (->Uniform root bf)))))
             {}
             variable-locations)]
    (->Uniforms root (atom us))))
