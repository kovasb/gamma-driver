(ns gamma.webgl.operators
  (:require
    [gamma.webgl.api :as p]))

(defn ->input [x]
  (reify p/IOperator
    (operate! [this target]
      (p/input! target x))))

(defrecord InputVectorVector [v type]
  p/IOperator
  (operate! [this target]
    (p/input!
      target
      (if (= type :uint16)
        (js/Uint16Array. (clj->js (flatten v)))
        (js/Float32Array. (clj->js (flatten v)))))))

(defn ->input-vector-vector
  ([v] (InputVectorVector. v :float32))
  ([v t] (InputVectorVector. v t)))


(defn ->input-vector [v]
  (reify p/IOperator
    (operate! [this target]
      (p/input! target (clj->js v)))))