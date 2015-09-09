(ns gamma.webgl.model.arraybuffer
  (:require [gamma.webgl.model.core :as m]))

(defn ->float32 [x]
  (js/Float32Array.
    (clj->js (flatten x))))


(defn arraybuffer-data [gl bo mode data]
  (.bindBuffer gl (.-ARRAY_BUFFER gl) bo)
  (.bufferData
    gl
    (.-ARRAY_BUFFER gl)
    data
    (.-STATIC_DRAW gl)))



(defrecord Arraybuffer [root parts compare bufferdata-fn]
  m/IModel
  (resolve [this val]
    (@parts val))
  (conform [this val]
    (reduce-kv
      (fn [_ k v]
        (let [p @parts]
          (case k
            :data (when (not (compare (:data p) v))
                    (arraybuffer-data (:gl root) (:object p) (:mode p) (bufferdata-fn v))
                    (swap! parts assoc :data v)))))
      nil
      val)))


(defn create-arraybuffer [root val]
  (let [o (.createBuffer (:gl root))]
    (->Arraybuffer root (atom (assoc val :object o)) = ->float32)))

