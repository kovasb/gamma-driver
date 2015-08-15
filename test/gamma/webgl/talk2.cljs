(ns gamma.webgl.talk2
  (:require [gamma.webgl.hello]))


(let [ops (r/draw [:root]
                  (shader/compile (example-shader)))
      driver (driver/driver
               {:gl (get-context "gl-canvas")}
               ops)]
  (driver/exec!
    driver
    {:hello-triangle {pos (->float32 [[1 0] [0 1] [1 -1]])}
     :draw           {:start 0 :count 3}}))



(def ops (r/draw [:root]
                 (shader/compile (example-shader))))


(def driver
  (driver/driver
    {:gl (get-context "gl-canvas")}
    ops))

(driver/exec!
  driver
  {:hello-triangle {pos
                    (->float32 [[-1 0] [0 1] [1 0]])}
   :draw           {:start 0 :count 3}})

(:init driver)
(:loop driver)