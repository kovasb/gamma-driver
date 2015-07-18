(ns gamma.webgl.driver
  (:require
    [gamma.webgl.interpreter :as itr]
    [gamma.webgl.operations :as ops]))

(defn driver [state ops]
  {:ops ops
   :init (mapcat identity (ops/initialization ops))
   :loop (ops/rewrite ops)
   :init? (atom false)
   :interpreter (itr/interpreter state)})

(defn exec! [driver data]
  (swap! (:state (:interpreter driver))
         assoc :root data)
  (when (not @(:init? driver))
    (itr/-eval (:interpreter driver) (:init driver))
    (reset! (:init? driver) true))
  (itr/-eval (:interpreter driver) (:loop driver)))

