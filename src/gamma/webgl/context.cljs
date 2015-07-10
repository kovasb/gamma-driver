(ns gamma.webgl.context
  (:require [gamma.webgl.api :as p]
            [goog.webgl :as ggl]))

(defrecord Context [node gl]
  p/IContext
  (gl [this] gl)
  (extensions [this] {}))

(defn context [node]
  (Context. node (.getContext node "webgl")))

