(ns gamma.webgl.model.renderbuffers
  (:require [gamma.webgl.model.core :as m]
            [gamma.webgl.model.arraybuffer :as ab]
            [gamma.webgl.platform.constants :as c]))



(defrecord Renderbuffer [root parts]
  m/IModel
  (resolve [this val]
    (@parts val))
  (conform [this val]))



(defn create-renderbuffer [root val]
  (let [gl (:gl root)
        obj (.createBuffer gl)
        {:keys [internalformat width height]} val]
    (.bindRenderbuffer
      gl
      (c/constants :renderbuffer)
      obj)
    (.renderbufferStorage
      gl
      (c/constants :renderbuffer)
      (c/constants internalformat)
      width
      height)
    (->Renderbuffer root (atom (assoc val :object o)))))



(defrecord Renderbuffers [root parts]
  m/IModel
  (resolve [this val]
    (if-let [a (@parts val)]
      a
      (let [a (create-renderbuffer root val)]
        (swap! parts assoc val a)
        a)))
  (conform [this val]
    (m/delegate m/conform #(m/resolve this %) val)))
