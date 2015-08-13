(ns gamma.webgl.tutorial
  (:require
    [gamma.api :as g]
    [gamma.webgl.api :as gd]
    [gamma.webgl.shader :as shader]
    [gamma.webgl.routines.basic :as r]
    [gamma.webgl.drivers.basic :as driver]
    [gamma.webgl.compiler.core]))


(def pos (g/attribute "posAttr" :vec2))

(defn example-shader []
  (shader/compile
    {:id              :hello-triangle
     :vertex-shader   {(g/gl-position) (g/vec4 pos 0 1)}
     :fragment-shader {(g/gl-frag-color) (g/vec4 1 0 0 1)}}))

;; Helpers
(defn get-context [id]
  (.getContext
    (.getElementById js/document id)
    "webgl"))

(defn ->float32 [x]
  (js/Float32Array.
    (clj->js (flatten x))))




(comment

  ;; fundamental thing is a sequence of commands

  (def commands
    (let [shader (shader/compile (example-shader))
         ab (gd/arraybuffer)
         attribute (assoc pos :shader (:id shader))
         data (->float32 [0 0 0 1 1 0])
         start 0
         count 3]
     [(gd/current-shader shader)
      (gd/bind-attribute attribute ab)
      (gd/bind-arraybuffer ab data)
      (gd/bind-framebuffer nil)
      (gd/draw-arrays start count)]))



  ;; driver takes commmands, compiles them, and sets up the context

  (def driver
    commands
    (driver/driver
      {:gl (get-context "gl-canvas")}))

  ;; execute the commands --> call into webgl, see something on screen

  (driver/exec! driver {})

  ;; compile separates out init (run once) vs loop (run many times)
  ;; (not a user api but helpful to understand)

  (def compiled (gamma.webgl.compiler.core/compile commands))

  (:init compiled)

  (:loop compiled)


  
  ;; routines abstract over ops


  (let [r (r/shader-draw (example-shader))
        driver (driver/driver
                 (:commands r)
                 {:gl (get-context "gl-canvas")})]
    (driver/exec!
      driver
      (driver/assoc-inputs
        (:inputs r)
        {:shader {pos (->float32 [0 0 1 0 0 1])}
         :draw {:start 0 :count 3}})))


  )