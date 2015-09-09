(ns gamma.webgl.model.attributes
  (:require [gamma.webgl.model.core :as m]))


(defn bind-attribute [gl location bo layout]
  (let [{:keys [size type normalized? stride offset]} layout]
    (.bindBuffer gl (.-ARRAY_BUFFER gl) bo)
    (.vertexAttribPointer
      gl
      location
      size
      type
      normalized?
      stride
      offset)
    (.enableVertexAttribArray gl location)))


(defn bind-attributes [root val locations]
  (reduce-kv
    (fn [_ k v]
      (bind-attribute
        (:gl root)
        (locations k)
        (m/resolve-in root [:arraybuffers (:arraybuffer v) :object])
        (:layout v)))
    nil
    val))

(defrecord Attributes [root parts variable-locations]
  m/IModel
  (resolve [this val]
    (@parts val))
  (conform [this val]
    (let [p @parts]
      (when (not= val p)
        (bind-attributes root val variable-locations)
        (reset! parts val)))))