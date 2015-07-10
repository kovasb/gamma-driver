(ns gamma.webgl.drivers.basic
  (:require
    [gamma.webgl.api :as gd]
    [gamma.webgl.api :as gdp]
    [goog.webgl :as ggl]))

(defrecord Driver []
  gdp/IDriver
  (exec! [this routine data]
    (reduce
      (fn [_ [target op]]
        (gdp/operate! op target))
      nil
      (gdp/ops routine data))))

(defn driver []
  (Driver.))






