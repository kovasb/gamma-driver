(ns gamma.webgl.model.programs
  (:require [gamma.webgl.model.core :as m]
            [gamma.webgl.model.program :as program]))



(defrecord Programs [root parts]
  m/IModel
  (resolve [this val]
    (let [p @parts]
      (if-let [x (p val)]
        x
        (let [x (program/create-program root val)]
          (swap! parts assoc val x)
          x))))
  (conform [this val]
    (m/delegate m/conform #(m/resolve this %) val)))