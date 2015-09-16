(ns gamma.webgl.model.program
  (:require [gamma.webgl.model.core :as m]
            [gamma.webgl.shader :as shader]
            [gamma.webgl.model.attributes :as attributes]
            [gamma.webgl.model.uniforms :as uniforms]))


(defrecord Program [root parts]
  m/IModel
  (resolve [this val]
    (@parts val))
  (conform [this val]
    (m/delegate m/conform @parts val)))


(defn variable-locations [gl shader obj]
  (reduce (fn [x y]
            (assoc
              x
              y
              (if (= :attribute (:storage y))
                (.getAttribLocation gl obj (:name y))
                (.getUniformLocation gl obj (:name y)))))
          {}
          (:inputs shader)))


(defn create-program [root val]
  (let [o (shader/install-shader (:gl root) val)
        variable-locations (variable-locations (:gl root) val o)]
    (Program.
      root
      (atom
        {:attributes (attributes/->Attributes
                       root
                       (atom {})
                       variable-locations)
         :uniforms (uniforms/uniforms
                     root
                     variable-locations)
         :object     o}))))