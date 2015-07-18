(ns gamma.webgl.test
  (:require
    [clojure.browser.repl :as repl]
    ))

(enable-console-print!)

(defonce conn
         (repl/connect "http://localhost:9000/repl"))

(comment
  (ns gamma.webgl.test1
    (:require
      [goog.dom :as gdom]
      [goog.webgl :as ggl]
      [gamma.api :as g]
      [gamma.program :as p]

      [gamma.webgl.shader :as shader]
      [gamma.webgl.interpreter :as itr]
      [gamma.webgl.operations :as ops]
      [cljs.pprint :as pprint]
      [gamma.webgl.routines.symbolic :as r]
      [gamma.webgl.driver :as driver]

      ))




  (def pos-attribute (g/attribute "posAttr" :vec2))


  (defn example-shader []
    (assoc
      (shader/Shader.
       (p/program
         {:id              :hello-triangle
          :vertex-shader   {(g/gl-position) (g/vec4 pos-attribute 0 1)}
          :fragment-shader {(g/gl-frag-color) (g/vec4 1 0 0 1)}}))
      :tag :shader))


  (defn get-context [id]
    (.getContext
      (.getElementById js/document id)
      "webgl"))

  (defn ->float32 [x]
    (js/Float32Array.
      (clj->js (flatten x))))

  (let [ops (r/draw [:root]
                    (example-shader))
        driver (driver/driver
                 {:gl (get-context "gl-canvas")}
                 ops)]
    (driver/exec!
      driver
      {:hello-triangle {pos-attribute
                        (->float32 [[-1 0] [0 1] [1 -1]])}
       :draw           {:start 0 :count 3}}))

 (comment
  (def ops
    (r/draw [:root]
            (example-shader)
            ))

  (def driver (driver/driver
                {:gl (get-context "gl-canvas")}
                ops))

  (:init? driver)
  (:loop driver)
  (:ops driver)

  (ops/rewrite (:ops driver))

  ;; implement:
  :bind-attribute
  :bind-uniform
  :bind-arraybuffer
  :draw-arrays




  (clojure.walk/postwalk #(if (vector? %)
                           (reverse %) %)
                         [[1 2] [3 4] [5 6]]
                         ))



  ;;;;;
  (def pos-varying (g/varying "posVarying" :vec2 :mediump))

  (defn example-shader2 []
    (p/program
      {:id              :ex2
       :precision       {:float :mediump}
       :vertex-shader   {(g/gl-position) (g/vec4 pos-attribute 0 1)
                         pos-varying     pos-attribute}
       :fragment-shader {(g/gl-frag-color)
                         (let [v (g/sin (g/* (g/* 4.0 (g/swizzle pos-varying :x))
                                             (g/* 4.0 (g/swizzle pos-varying :y)))
                                        )]
                           (g/if (g/< 0 v)
                             (g/vec4 0 0 v 1)
                             (g/vec4 0 (g/abs v) 0 1)))}}))

  (defn example-shader2 []
    (p/program
      {:id              :ex2
       :precision       {:float :mediump}
       :vertex-shader   {(g/gl-position) (g/vec4 pos-attribute 0 1)
                         pos-varying     pos-attribute}
       :fragment-shader {(g/gl-frag-color)
                         (let [v (g/div
                                   (g/+ (g/cos (g/* 10 (g/swizzle pos-varying :x)))
                                        (g/cos (g/* 10 (g/swizzle pos-varying :y))))
                                   2)]
                           (g/if (g/< 0 v)
                             (g/vec4 0 0 v 1)
                             (g/vec4 0 (g/abs v) 0 1)))}}))

  (comment
    )

  (run (example-shader2)
       {:data {pos-attribute {:tag :data :data (js/Float32Array. (clj->js [-1 -1 1 -1 -1 1 1 1 1 -1 -1 1]))}}
        :draw {:start 0 :count 6}})

  (run (example-shader)
       {:data {pos-attribute {:tag :data :data (js/Float32Array. (clj->js [-1 0 0 1 1 0]))}}
        :draw {:start 0 :count 3}})


  (println (:glsl (:fragment-shader (example-shader2)))))
