(ns gamma.webgl.compiler.draw
  (:require
    [gamma.webgl.api :as p]))


(defn draw-arrays [mode first count]
  [[:drawArrays :gl mode first count]])

(defn draw-elements [mode first count offset]
  [[:drawElements :gl mode first count offset]])

(defn draw-arrays-instanced [mode first count]
  [[:drawArraysInstanced :gl mode first count]])

;; make extension visible in env

