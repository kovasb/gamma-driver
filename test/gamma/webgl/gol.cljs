(ns gamma.webgl.gol
  (:require
    [gamma.api :as g]
    [gamma.webgl.api :as gd]
    [gamma.webgl.shader :as shader]
    [gamma.webgl.routines.basic :as r]
    [gamma.webgl.drivers.basic :as driver]
    [gamma.webgl.compiler.core]
    [gamma.webgl.platform.constants :as c]
    [gamma.webgl.compiler.texture :as tex]))


(defn get-context [id]
  (.getContext
    (.getElementById js/document id)
    "webgl"))

(defn ->float32 [x]
  (js/Float32Array.
    (clj->js (flatten x))))


(def a-position (g/attribute "a_Position" :vec2))

(def a-tex-coord (g/attribute "a_TexCoord" :vec2))

(def v-tex-coord (g/varying "v_TexCoord" :vec2 :mediump))

(def u-sampler (g/uniform "u_Sampler" :sampler2D))


(defn init-framebuffer [fb tex rb width height pixels]
  [(tex/wrap-s tex ::c/repeat)
   (tex/wrap-t tex ::c/repeat)
   (tex/min-filter tex ::c/linear)
   (tex/mag-filter tex ::c/linear)
   (gd/renderbufferStorage
     {:renderbuffer rb}
     {:width width :height height
      :internalformat ::c/depth-component16})
   (gd/framebufferRenderbuffer
     {:framebuffer fb}
     {:attachment   ::c/depth-attachment
      :renderbuffer rb})
   (tex/texImage2D-2
     tex
     {:width width :height height
      :format ::c/rgba
      :type ::c/unsigned-byte :pixels pixels})
   (gd/framebufferTexture2D
     {:framebuffer fb}
     {:attachment ::c/color-attachment0
      :texture    (:texture tex)})])

;;; GOL SHADER

(defn gol-rule [center total]
  (g/if (g/== center 1)
    (g/if (g/< total 3)
      0
      (g/if (g/> total 4)
        0
        1))
    (g/if (g/== total 3)
      1
      0)))

(defn gol-rule [center total]
  (g/if (g/< total 3)
    0.4
    (g/if (g/> total 4)
      0.8
      0.1)))

(defn moore-neighborhood [u-sampler center step]
  (for [x [(g/* -1 step) 0 step]
        y [(g/* -1 step) 0 step]]
    (g/texture2D u-sampler (g/+ center (g/vec2 x y)))))

(def nb (map
          #(g/swizzle % :x)
          (moore-neighborhood
            u-sampler
            v-tex-coord
            (/ 1.0 512))))

(def tot (apply g/+ nb))


(def gol-rule-val
  (let [
        total (apply g/+ nb)]
    (gol-rule (nth nb 5) total)))


(defn gol-shader []
  (shader/compile
    {:id              :gol
     :vertex-shader   {(g/gl-position) (g/vec4 a-position 0 1)
                       v-tex-coord     a-tex-coord}
     :fragment-shader {(g/gl-frag-color)
                       (g/vec4
                         gol-rule-val 0 0 0.9)}
     :precision       {:float :mediump}}))

(defn gol-shader []
  (shader/compile
    {:id              :gol
     :vertex-shader   {(g/gl-position) (g/vec4 a-position 0 1)
                       v-tex-coord     a-tex-coord}
     :fragment-shader {(g/gl-frag-color)
                       (g/vec4
                         (g/swizzle (g/sampler2D u-sampler v-tex-coord) :x) 0 0 0.9)}
     :precision       {:float :mediump}}))

(gol-shader)

(defn screen-shader []
  (shader/compile
    {:id :screen-shader
     :vertex-shader
                      {(g/gl-position) (g/vec4 a-position 0 1)
                       v-tex-coord     a-tex-coord}
     :fragment-shader {(g/gl-frag-color)
                       (g/vec4
                         (g/swizzle
                           (g/texture2D u-sampler v-tex-coord)
                           :x)
                         0 0 0.9)}
     :precision       {:float :mediump}}
  ))

(println (:glsl (:vertex-shader (screen-shader))))

(println (:glsl (:fragment-shader (gol-shader))))

;;;;;


(def t1 (gd/texture))
(def t2 (gd/texture))

(def tex1 {:texture t1 :texture-unit 0})
(def tex2 {:texture t2 :texture-unit 1})


(def fb1 (gd/framebuffer))
(def fb2 (gd/framebuffer))
(def rb1 (gd/renderbuffer))
(def rb2 (gd/renderbuffer))


(def gol-init-array (js/Uint8Array. (* 512 512 4)))

(loop [x 0]
  (if (>= x (* 512 512 4))
    nil
    (do
      (aset gol-init-array x (* 255 (rand-int 2)))
      (recur (+ x 4)))))

(* 512 512)

(.-length gol-init-array)


(def fb-init1 (init-framebuffer fb1 tex1 rb1 512 512 gol-init-array))
(def fb-init2 (init-framebuffer fb2 tex2 rb2 512 512 nil))

(def golshader (gol-shader) )

(def gol-draw1 (r/shader-draw golshader {u-sampler tex1} fb2))

(def gol-draw2 (r/shader-draw golshader {u-sampler tex2} fb1))

(def screen-draw (r/shader-draw (screen-shader) {u-sampler tex2} nil))


(def commands
  [fb-init1
   fb-init2
   (:commands gol-draw1)
   (:commands screen-draw)
   ])


(def d (driver/driver
         commands
         {:gl (get-context "gl-canvas")}))


(driver/exec!
  d
  (driver/assoc-inputs
    {:stage1 (:inputs gol-draw1)
     :stage2 (:inputs screen-draw)
     :stage3 (:inputs gol-draw2)}
    {
     :stage1 {:shader {a-tex-coord (->float32 [[0 1] [0 0] [1 1]
                                               [0 0] [1 1] [1 0]])
                       a-position  (->float32 [
                                               [[-1 1] [-1 -1] [1 1]
                                                [-1 -1] [1 1] [1 -1]]
                                               ])}
              :draw {:start 0 :count 6}}
     :stage3 {:shader {a-tex-coord (->float32 [[0 1] [0 0] [1 1]
                                               [0 0] [1 1] [1 0]])
                       a-position  (->float32 [
                                               [[-1 1] [-1 -1] [1 1]
                                                [-1 -1] [1 1] [1 -1]]
                                               ])}
              :draw {:start 0 :count 6}}
     :stage2 {:shader {a-tex-coord (->float32 [[0 1] [0 0] [1 1]
                                               [0 0] [1 1] [1 0]])
                       a-position  (->float32 [
                                               [[-1 1] [-1 -1] [1 1]
                                                [-1 -1] [1 1] [1 -1]]
                                               ])}
              :draw   {:start 0 :count 6}}}))

(defn state-lookup [driver val]
  (@(:state (:interpreter driver)) val))

(state-lookup d fb1)

(.bindFramebuffer
  (state-lookup d :gl)
  (c/constants ::c/framebuffer)
  (state-lookup d fb1))


(def fb1-pixels (js/Uint8Array. (* 512 512 4 )))

(.readPixels
  (state-lookup d :gl)
  0
  0
  512
  512
  (c/constants ::c/rgba)
  (c/constants ::c/unsigned-byte)
  fb1-pixels)


(for [x (range 100)] (aget fb1-pixels x))
(for [x (range 100)] (aget gol-init-array x))

(.bindFramebuffer
  (state-lookup d :gl)
  (c/constants ::c/framebuffer)
  (state-lookup d fb2)
  )

(def fb2-pixels (js/Uint8Array. (* 512 512 4)))

(.readPixels
  (state-lookup d :gl)
  0
  0
  512
  512
  (c/constants ::c/rgba)
  (c/constants ::c/unsigned-byte)
  fb2-pixels)


(for [x (range 1000)] (aget fb2-pixels x))

;;
(.bindFramebuffer
  (state-lookup d :gl)
  (c/constants ::c/framebuffer)
  nil
  )

(def screen-pixels (js/Uint8Array. (* 512 512 4)))

(.readPixels
  (state-lookup d :gl)
  0
  0
  512
  512
  (c/constants ::c/rgba)
  (c/constants ::c/unsigned-byte)
  screen-pixels)


(for [x (range 1000)] (aget screen-pixels x))