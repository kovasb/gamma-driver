(ns gamma.webgl.hello
  (:require
    [cljs.pprint :as pprint]
    [gamma.api :as g]
    [gamma.webgl.shader :as shader]
    [gamma.webgl.routines.symbolic :as r]
    [gamma.webgl.driver :as driver]))

;; Shader definition
(def pos (g/attribute "posAttr" :vec2))

(defn example-shader []
  {:id              :hello-triangle
   :vertex-shader   {(g/gl-position) (g/vec4 pos 0 1)}
   :fragment-shader {(g/gl-frag-color) (g/vec4 1 0 0 1)}})

;; Helpers
(defn get-context [id]
  (.getContext
    (.getElementById js/document id)
    "webgl"))

(defn ->float32 [x]
  (js/Float32Array.
    (clj->js (flatten x))))

;; Draw
(comment
  (let [ops (r/draw [:root]
                   (shader/compile (example-shader)))
       driver (driver/driver
                {:gl (get-context "gl-canvas")}
                ops)]
   (driver/exec!
     driver
     {:hello-triangle {pos
                       (->float32 [[1 0] [0 1] [1 1]])}
      :draw           {:start 0 :count 3}}))

  )

(comment
 (require 'gamma.webgl.hello)
 (in-ns 'gamma.webgl.hello)


  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

  (def ops (r/draw [:root]
               (shader/compile (example-shader))))

  (def driver
    (driver/driver
         {:gl (get-context "gl-canvas")}
         ops))

  (driver/exec!
    driver
    {:hello-triangle {pos
                      (->float32 [[-1 0] [0 1] [1 -1]])}
     :draw           {:start 0 :count 3}})

  (:init driver)
  (:loop driver)

 (require '[gamma.webgl.interpreter :as itr])
 (itr/-eval (:interpreter driver) :gamma.webgl.constants/array-buffer)
 (itr/-eval (:interpreter driver) {:tag :arraybuffer, :id 2})





  )