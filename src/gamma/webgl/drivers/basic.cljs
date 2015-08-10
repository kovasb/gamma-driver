(ns gamma.webgl.drivers.basic
  (:require
    [gamma.webgl.interpreter :as itr]
    [gamma.webgl.compiler.core :as compiler]))

(defn driver [state ops]
  {:ops ops
   :init (mapcat identity (compiler/compile-init ops))
   :loop (compiler/compile-loop ops)
   :init? (atom false)
   :interpreter (itr/interpreter state)})

(defn exec! [driver data]
  (swap! (:state (:interpreter driver))
         assoc :root data)
  (when (not @(:init? driver))
    (itr/-eval (:interpreter driver) (:init driver))
    (reset! (:init? driver) true))
  (itr/-eval (:interpreter driver) (:loop driver)))

