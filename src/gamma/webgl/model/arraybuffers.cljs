(ns gamma.webgl.model.arraybuffers
  (:require [gamma.webgl.model.core :as m]
            [gamma.webgl.model.arraybuffer :as ab]))



(defrecord Arraybuffers [root parts]
  m/IModel
  (resolve [this val]
    (if-let [a (@parts val)]
      a
      (let [a (ab/create-arraybuffer root val)]
        (swap! parts assoc val a)
        a)))
  (conform [this val]
    (m/delegate m/conform #(m/resolve this %) val)))
