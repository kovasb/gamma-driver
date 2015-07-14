(ns gamma.webgl.context
  (:require [gamma.webgl.api :as p]
            [goog.webgl :as ggl]))

(defrecord Context [node gl extensions]
  p/IContext
  (gl [this] gl)
  (extensions [this] extensions))

(defn context
  ([node] (context node []))
  ([node extensions]
   (let [gl (.getContext node "webgl")])
   (Context. node gl
             (into {}
                   (map (fn [x] [x (.getExtension gl x)]) extensions)))))

