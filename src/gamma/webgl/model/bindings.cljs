(ns gamma.webgl.model.bindings
  (:require [gamma.webgl.model.core :as m]))



(defrecord GlobalBindings [root parts]
  m/IModel
  (resolve [this v]
    (@parts v))
  (conform [this val]
    (reduce-kv
      (fn [_ k v]
        (case k
          :program
          (when (not= v (m/resolve this k))
            (.useProgram
              (:gl root)
              (m/resolve-in root [:programs v :object]))
            (swap! parts assoc k v))))
      nil
      val)))