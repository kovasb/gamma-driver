(ns gamma.webgl.tutorial
  (:require
    [gamma.api :as g]
    [gamma.webgl.api :as gd]
    [gamma.webgl.shader :as shader]
    [gamma.webgl.routines.basic :as r]
    [gamma.webgl.drivers.basic :as driver]
    [gamma.webgl.compiler.core]
    [gamma.webgl.platform.constants :as c]))


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

(defn state-lookup [driver val]
  (@(:state (:interpreter driver)) val))




(comment


  ;; fundamental thing is a sequence of commands

  (require '[gamma.webgl.platform.constants :as c])

  (def commands
    (let [shader (example-shader)
         ab (gd/arraybuffer)
         attribute (assoc pos :shader (:id shader))
         data (->float32 [0 0 0 1 -1 0])
         start 0
         count 3
          location {:tag :location :variable attribute}]
     [
      (gd/vertexAttribPointer
        ab
        (assoc (r/default-layout attribute) :index location))
      (gd/enableVertexAttribArray {:index location})
      (gd/bufferData ab {:data data :usage ::c/static-draw})

      (gd/drawArrays shader nil {:mode ::c/triangles :start start :count count})
      ]))




  ;; driver takes commmands, compiles them, and sets up the context

  (def driver
    (driver/driver
      commands
      {:gl (get-context "gl-canvas")}))

  ;; execute the commands --> call into webgl, see something on screen

  (driver/exec! driver {})

  ;; ELEMENT-ARRAYBUFFER

  (def commands
    (let [shader (example-shader)
          ab (gd/arraybuffer)
          ea (gd/element-arraybuffer)
          attribute (assoc pos :shader (:id shader))
          data (->float32 [0 0
                           0 1
                           -1 0
                           1 0])
          data-2 (js/Uint8Array. #js [0 1 2 0 1 3])
          start 0
          count 6
          location {:tag :location :variable attribute}]
      [
       (gd/vertexAttribPointer
         ab
         (assoc (r/default-layout attribute) :index location))
       (gd/enableVertexAttribArray {:index location})
       (gd/bufferData ab {:data data :usage ::c/static-draw})
       (gd/bufferData ea {:data data-2 :usage ::c/static-draw})
       (gd/drawElements {:program shader :framebuffer nil :element-arraybuffer ea}
                        {:mode ::c/triangles :type ::c/unsigned-byte :count count :offset 0})
       ]))

  (last commands)

  (defn state-lookup [driver val]
    (@(:state (:interpreter driver)) val))

  (state-lookup driver {:tag :element-arraybuffer, :id 15})



  (c/constants ::c/unsigned-byte)

  (def driver
    (driver/driver
      commands
      {:gl (get-context "gl-canvas")}))
  (driver/exec! driver {})

  ;; INSTANCED ARRAYS

  (def pos (g/attribute "posAttr" :vec2))
  (def xoffset (g/attribute "posOffset" :float))

  (defn instanced-shader []
    (shader/compile
      {:id              :instanced
       :vertex-shader   {(g/gl-position) (g/vec4 (g/+ xoffset (g/swizzle pos :x))
                                                 (g/swizzle pos :y)
                                                 0 1)}
       :fragment-shader {(g/gl-frag-color) (g/vec4 1 0 0 1)}}))


  (def commands
    (let [shader (instanced-shader)

          ab1 (gd/arraybuffer)
          attr1(assoc pos :shader (:id shader))
          data1 (->float32 [0 0 0 1 -1 0])
          l1 {:tag :location :variable attr1}

          ab2 (gd/arraybuffer)
          attr2 (assoc xoffset :shader (:id shader))
          data2 (->float32 [0.1 0.2 0.3])
          l2 {:tag :location :variable attr2}

          start 0
          count 3
          ]
      [
       (gd/vertexAttribPointer
         ab1
         (assoc (r/default-layout attr1) :index l1))
       (gd/enableVertexAttribArray {:index l1})
       (gd/bufferData ab1 {:data data1 :usage ::c/static-draw})

       (gd/vertexAttribPointer
         ab2
         (assoc (r/default-layout attr2) :index l2))
       (gd/enableVertexAttribArray {:index l2})
       (gd/bufferData ab2 {:data data2 :usage ::c/static-draw})
       (gd/vertexAttribDivisorANGLE ab2 {:index l2 :count 1})


       (gd/drawArraysInstancedANGLE shader nil
                                    {:mode ::c/triangles :start start :count count :primcount 3})
       ]))

  (def driver
    (driver/driver
      commands
      {:gl (get-context "gl-canvas")}))
  (driver/exec! driver {})

  (:init driver)

  (state-lookup driver {:tag :arraybuffer, :id 22})


  (.getExtension (get-context "gl-canvas") "ANGLE_instanced_arrays")

  ;; we want variables (called inputs) in our commands

  (def pos-input (gd/input))
  (def start-input (gd/input))
  (def count-input (gd/input))

  (def commands
    (let [shader (shader/compile (example-shader))
          ab (gd/arraybuffer)
          attribute (assoc pos :shader (:id shader))]
      [(gd/current-shader shader)
       (gd/bind-attribute attribute ab)
       (gd/bind-arraybuffer ab pos-input)
       (gd/bind-framebuffer nil)
       (gd/draw-arrays start-input count-input)]))

  (def driver
    (driver/driver
      commands
      {:gl (get-context "gl-canvas")}))

  (driver/exec!
    driver
    {pos-input    (->float32 [0 0 0 1 1 0])
     start-input 0
     count-input 3})


  ;; routines abstract over commands and data input


  (let [r (r/shader-draw (example-shader) {} nil)
        driver (driver/driver
                 (:commands r)
                 {:gl (get-context "gl-canvas")})]
    (driver/exec!
      driver
      (driver/assoc-inputs
        (:inputs r)
        {:shader {pos (->float32 [0 0 1 0 0 -1])}
         :draw   {:start 0 :count 3}})))


  )
