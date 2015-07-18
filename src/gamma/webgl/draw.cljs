(ns gamma.webgl.draw
  (:require
    [gamma.webgl.api :as p]))


(defn draw-arrays [mode first count]
  [[:drawArrays :gl mode first count]])

(defn draw-elements [mode first count offset]
  [[:drawElements :gl mode first count offset]])


