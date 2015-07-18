(ns gamma.webgl.compiler
  (:require
    [gamma.webgl.routines.symbolic :as r]
    [gamma.webgl.operations :as ops]))

(comment
  (def x
    (let [a1 {:tag :variable
              :storage :attribute
              :name "foo"
              :type :vec2}]
      (r/draw
        {:tag :shader :id :s1 :inputs [a1]}
        {:data {a1 {:tag :data :data :a1-data}}
         :draw {:start 0 :count 3}})))

  (require '[gamma.webgl.operations :as ops] :reload)

  (require '[gamma.webgl.shader :as shader])

  (let [a1 {:tag :variable
            :storage :attribute
            :name "foo"
            :type :vec2}]
    (shader/init-shader {:tag :shader :id :s1 :inputs [a1]}))

  (ops/instructions ops/rules x)

  (ops/initialization ops/inits x)

  ( (set (apply concat x)))

  (ops/map-match? )






  )