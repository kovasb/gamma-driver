(ns gamma.webgl.image
  (:require
    [gamma.api :as g]
    [gamma.webgl.api :as gd]
    [gamma.webgl.shader :as shader]
    [gamma.webgl.routines.basic :as r]
    [gamma.webgl.drivers.basic :as driver]
    [gamma.webgl.compiler.core]
    [gamma.webgl.platform.constants :as c]
    [gamma.webgl.compiler.texture :as tex]))

(def a-position (g/attribute "a_Position" :vec2))

(def a-tex-coord (g/attribute "a_TexCoord" :vec2))

(def v-tex-coord (g/varying "v_TexCoord" :vec2 :mediump))

(def u-sampler (g/uniform "u_Sampler" :sampler2D))



(defn example-shader []
  (shader/compile
    {:id              :hello-triangle
     :vertex-shader   {(g/gl-position) (g/vec4 a-position 0 1)
                       v-tex-coord     a-tex-coord}
     :fragment-shader {(g/gl-frag-color) (g/texture2D u-sampler v-tex-coord)}}))




(defn get-context [id]
  (.getContext
    (.getElementById js/document id)
    "webgl"))

(.-clientWidth (.getElementById js/document "gl-canvas"))
(.-width (.getElementById js/document "gl-canvas"))

(set! (.-width (.getElementById js/document "gl-canvas")) 512)

(defn ->float32 [x]
  (js/Float32Array.
    (clj->js (flatten x))))


(defn example-data [image]
  {a-position  [[-0.5 0.5] [-0.5 -0.5] [0.5 0.5]
                [-0.5 -0.5] [0.5 0.5] [0.5 -0.5]]
   a-tex-coord [[0 1] [0 0] [1 1]
                [0 0] [1 1] [1 0]]
   u-sampler   {:data     image
                :unpack   {:flip-y true}
                :filter   {:min :linear :mag :nearest}
                :id       0}
   })


(comment

  (def t (gd/texture))

  (def tex {:texture t :texture-unit 0})

  (do
    (def pixels
     (for [x (range 0 (* 512 512))]
       (let [y (* 255 (mod x 2))]
         [0 y 0 255])))
    1)

  (count pixels)
  (* 512 512)


  (def pixelsa (js/Uint8Array. (clj->js (flatten pixels))))

  (aget pixelsa 9)

  (def texture-init
    (let [width 512 height 512]
      [
       (tex/wrap-s tex ::c/repeat)
      (tex/wrap-t tex ::c/repeat)
      (tex/min-filter tex ::c/linear)
      (tex/mag-filter tex ::c/linear)
      (tex/texImage2D-2
        tex
        {:width  width :height height
         :format ::c/rgba
         :type   ::c/unsigned-byte :pixels pixelsa})]))


  (def shader-draw (r/shader-draw (example-shader) {u-sampler tex} nil))


  (def d (driver/driver
           (concat texture-init (:commands shader-draw))
           {:gl (get-context "gl-canvas")}))

  (driver/exec!
    d
    (driver/assoc-inputs
      (:inputs shader-draw)
      {:shader {a-tex-coord (->float32 [[0 1] [0 0] [1 1]
                                        [0 0] [1 1] [1 0]])
                a-position  (->float32 [
                                        [[-1 1] [-1 -1] [1 1]
                                         [-1 -1] [1 1] [1 -1]]
                                        ])}
       :draw   {:start 0 :count 6}}))

  ;;;;;; CONVOLUTIONS

  (defn convolve [a b]
    (apply g/+ (map g/* a b)))

  (defn moore-neighborhood [u-sampler center step]
    (for [x [(g/* -1 step) 0 step]
          y [(g/* -1 step) 0 step]]
      (g/texture2D u-sampler (g/+ center (g/vec2 x y)))))

  (for [ x [:a :b :c] y [:1 :2 :3]] [x y])

  (def kernel-blur (flatten [[1 1 1] [1 1 1] [1 1 1]]))

  (def kernel-edge (flatten [[0 1 0] [1 -4 1] [0 1 0]]))

  (convolve kernel-blur
            (moore-neighborhood
              u-sampler
              v-tex-coord
              (/ 1.0 512)))


  (def a-position (g/attribute "a_Position" :vec2))

  (def a-tex-coord (g/attribute "a_TexCoord" :vec2))

  (def v-tex-coord (g/varying "v_TexCoord" :vec2 :mediump))

  (def u-sampler (g/uniform "u_Sampler" :sampler2D))


  (def blur-frag
    (g/div (convolve kernel-blur
                     (moore-neighborhood
                       u-sampler
                       v-tex-coord
                       (/ 1.0 512)))
           (g/vec4 9.0 9.0 9.0 9.0)))

  (def edge-frag
    (convolve kernel-edge
              (moore-neighborhood
                u-sampler
                v-tex-coord
                (/ 1.0 512))))

  (defn example-shader []
    (shader/compile
      {:id              :hello-triangle
       :vertex-shader   {(g/gl-position) (g/vec4 a-position 0 1)
                         v-tex-coord     a-tex-coord}
       :fragment-shader {(g/gl-frag-color)
                         (g/vec4
                           (g/swizzle
                             edge-frag
                            :xyz) 1)}
       :precision       {:float :mediump}}))

  (println (:glsl (:fragment-shader (example-shader))))



  ;;;;

  (def t (gd/texture))

  (def tex {:texture t :texture-unit 0})

  (def image (js/Image.))
  (aset image "onload" (fn [] (js/alert "image loaded")))
  (aset image "src" "lenna.png")

  (def texture-init
    (tex/texture-image
      tex
      {:target ::c/texture-2d
       :format ::c/rgb
       :type   ::c/unsigned-byte
       :s      ::c/repeat
       :t      ::c/repeat
       :min    ::c/linear
       :mag    ::c/linear
       :data   image}))

  (def shader-draw (r/shader-draw (example-shader) {u-sampler tex} nil))


  (def d (driver/driver
           (concat texture-init (:commands shader-draw))
           {:gl (get-context "gl-canvas")}))

  (driver/exec!
    d
    (driver/assoc-inputs
      (:inputs shader-draw)
      {:shader {a-position  (->float32 [[-1 1] [-1 -1] [1 1]
                                        [-1 -1] [1 1] [1 -1]])
                a-tex-coord (->float32 [[0 1] [0 0] [1 1]
                                        [0 0] [1 1] [1 0]])}
       :draw   {:start 0 :count 6}}))


  ;;;;;;;;;;;;;;;;;;;;;;;;;;; Game of Life

  ;; get moore nbhood




  ;; init 2 FBOs



  ;; draw from FBO tex1 to FBO2

  ;; render from FBO2 tex



  )