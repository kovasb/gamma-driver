(ns gamma.webgl.framebuffer
  (:require
    [gamma.api :as g]
    [gamma.webgl.api :as gd]
    [gamma.webgl.shader :as shader]
    [gamma.webgl.routines.basic :as r]
    [gamma.webgl.drivers.basic :as driver]
    [gamma.webgl.compiler.core]
    [gamma.webgl.compiler.texture :as tex]
    [gamma.webgl.platform.constants :as c]))


;; EX1

(def pos (g/attribute "posAttr" :vec2))

(defn shader1 []
  (shader/compile
    {:id              :hello-triangle
    :vertex-shader   {(g/gl-position) (g/vec4 pos 0 1)}
    :fragment-shader {(g/gl-frag-color) (g/vec4 1 0 0 1)}}))

;; EX2

(def a-position (g/attribute "a_Position" :vec2))

(def a-tex-coord (g/attribute "a_TexCoord" :vec2))

(def v-tex-coord (g/varying "v_TexCoord" :vec2 :mediump))

(def u-sampler (g/uniform "u_Sampler" :sampler2D))


(defn shader2 []
  (shader/compile
    {:id :texture-test
     :vertex-shader
                      {(g/gl-position) (g/vec4 a-position 0 1)
                       v-tex-coord     a-tex-coord}
     :fragment-shader {(g/gl-frag-color) (g/texture2D u-sampler v-tex-coord)}}))


(defn get-context [id]
  (.getContext
    (.getElementById js/document id)
    "webgl"))

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
                :id       0}})


(defn init-framebuffer [fb tex rb width height]
  [(tex/min-filter tex ::c/linear)
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
      :type ::c/unsigned-byte :pixels nil})
   (gd/framebufferTexture2D
     {:framebuffer fb}
     {:attachment ::c/color-attachment0
      :texture    (:texture tex)})])


(comment
  (def t (gd/texture))

  (def tex {:texture t :texture-unit 0})

  (def fb (gd/framebuffer))
  (def rb (gd/renderbuffer))

  (def fb-init (init-framebuffer fb tex rb 512 512))

  (def shader-draw1 (r/shader-draw (shader1) {} fb))

  (def shader-draw2 (r/shader-draw (shader2) {u-sampler tex} nil))


  (def commands
    [fb-init
     (:commands shader-draw1)
     (:commands shader-draw2)])


  (def d (driver/driver
           commands
           {:gl (get-context "gl-canvas")}))

  {:stage1 (:inputs shader-draw1)
   :stage2 (:inputs shader-draw2)}

  (driver/exec!
    d
    (driver/assoc-inputs
      {:stage1 (:inputs shader-draw1)
       :stage2 (:inputs shader-draw2)}
      {:stage1 {:shader {pos (->float32 [[-1 1] [-1 -1] [1 1]])}
                :draw {:start 0 :count 3}}

       :stage2 {:shader {a-position  (->float32 [[-0.5 0.5] [-0.5 -0.5] [0.5 0.5]
                                         [-0.5 -0.5] [0.5 0.5] [0.5 -0.5]])
                 a-tex-coord (->float32 [[0 1] [0 0] [1 1]
                                         [0 0] [1 1] [1 0]])}
        :draw   {:start 0 :count 6}}}))



  (defn state-lookup [driver val]
    (@(:state (:interpreter driver)) val))

  (def v {:tag :location, :variable {:tag :variable, :name "u_Sampler", :type :sampler2D, :storage :uniform, :shader :texture-test}})

  (state-lookup d fb)

  (.checkFramebufferStatus
    (state-lookup d :gl)
    (c/constants ::c/framebuffer))

  (:init d)
  (:tag (example-shader))


  (def texture-init
    (tex/texture-image
      tex
      {:target ::c/texture-2d
       :format ::c/rgba
       :type   ::c/unsigned-byte
       :s      ::c/repeat
       :t      ::c/repeat
       :min    ::c/linear
       :mag    ::c/linear
       :data   image}))




  )



(comment
  (defn example-driver []
   (let [c (.getContext (.getElementById js/document "gl-canvas") "webgl")]
     (.enable c (.-DEPTH_TEST c))
     (.clear c (.-DEPTH_BUFFER_BIT c))
     (driver/basic-driver c))))


(comment
  (defn main []
   (let [image (js/Image.)
         d (example-driver)
         p (example-program)]
     (aset image "onload"
           (fn [] (gd/draw-arrays
                    d
                    (gd/bind d p (example-data image))
                    {})))
     (aset image "src" "nehe.png"))))