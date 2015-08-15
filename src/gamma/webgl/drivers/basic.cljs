(ns gamma.webgl.drivers.basic
  (:require
    [gamma.webgl.interpreter :as itr]
    [gamma.webgl.compiler.core :as compiler]
    [gamma.webgl.api :as gd]))

(defn driver [ops state]
  {:ops ops
   :init (mapcat identity (compiler/compile-init ops))
   :loop (compiler/compile-loop ops)
   :init? (atom false)
   :interpreter (itr/interpreter state)})

(defn exec! [driver data]
  (swap! (:state (:interpreter driver))
         merge data)
  (when (not @(:init? driver))
    (itr/-eval (:interpreter driver) (:init driver))
    (reset! (:init? driver) true))
  (itr/-eval (:interpreter driver) (:loop driver)))


(defn input? [x]
  (if (map? x)
    (= (:tag x) ::gd/input)))

(defn assoc-inputs* [x y result]
  (if (input? x)
    (swap! result assoc x y)
    (if (map? x)
      (merge-with (fn [x y] (assoc-inputs* x y result)) x y)
      nil)))

(defn assoc-inputs [x y]
  (let [z (atom {})]
    (assoc-inputs* x y z)
    @z))

