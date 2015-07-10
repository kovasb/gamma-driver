(ns gamma.webgl.operators
  (:require
    [gamma.webgl.api :as p]))

(defn ->input [x]
  (reify p/IOperator
    (operate! [this target]
      (p/input! target x))))

(defn ->input-vector-vector [v]
  (reify p/IOperator
    (operate! [this target]
      (p/input! target (js/Float32Array. (clj->js (flatten v)))))))

(defn ->input-vector [v]
  (reify p/IOperator
    (operate! [this target]
      (p/input! target (clj->js v)))))