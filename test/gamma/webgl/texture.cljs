(ns gamma.webgl.texture
  (:require
    [gamma.api :as g]
    [gamma.webgl.api :as gd]
    [gamma.webgl.shader :as shader]
    [gamma.webgl.routines.basic :as r]
    [gamma.webgl.drivers.basic :as driver]
    [gamma.webgl.compiler.core]
    [gamma.webgl.compiler.texture :as tex]
    [gamma.webgl.platform.constants :as c]))


(def a-position (g/attribute "a_Position" :vec2))

(def a-tex-coord (g/attribute "a_TexCoord" :vec2))

(def v-tex-coord (g/varying "v_TexCoord" :vec2 :mediump))

(def u-sampler (g/uniform "u_Sampler" :sampler2D))


(defn example-shader []
  (shader/compile
    {:id :texture-test
     :vertex-shader
                      {(g/gl-position) (g/vec4 a-position 0 1)
                       v-tex-coord     a-tex-coord}
     :fragment-shader {(g/gl-frag-color) (g/texture2D u-sampler v-tex-coord)}}))

(defn texture-shader [a-position a-tex-coord u-sampler]
  (let [v-tex-coord (g/varying "v_TexCoord" :vec2 :mediump)]
    (shader/compile
      {:id :texture-shader
       :vertex-shader
                        {(g/gl-position) (g/vec4 a-position 0 1)
                         v-tex-coord     a-tex-coord}
       :fragment-shader {(g/gl-frag-color) (g/texture2D u-sampler v-tex-coord)}})))


(defn get-context [id]
  (.getContext
    (.getElementById js/document id)
    "webgl"))

(defn ->float32 [x]
  (js/Float32Array.
    (clj->js (flatten x))))




(comment
  (def t (gd/texture))

  (def tex {:texture t :texture-unit 0})

  (def image (js/Image.))
  (aset image "onload" (fn [] (js/alert "image loaded")))
  (aset image "src" "nehe.gif")

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

  (def shader-draw (r/shader-draw (example-shader) {u-sampler tex} nil))


  (def d (driver/driver
           (concat texture-init (:commands shader-draw))
           {:gl (get-context "gl-canvas")}))

  (do
    (driver/exec!
     d
     (driver/assoc-inputs
       (:inputs shader-draw)
       {:shader {a-position  (->float32 [[-1 1] [-1 -1] [1 1]
                                         [-1 -1] [1 1] [1 -1]])
                 a-tex-coord (->float32 [[0 1] [0 0] [1 1]
                                         [0 0] [1 1] [1 0]])}
        :draw   {:start 0 :count 6}}))
    (.readPixels
      (state-lookup d :gl)
      0
      0
      512
      512
      (c/constants ::c/rgba)
      (c/constants ::c/unsigned-byte)
      screen-pixels))

  (defn state-lookup [driver val]
    (@(:state (:interpreter driver)) val))


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


  (for [x (range 5000)] (aget screen-pixels x))


  (def v {:tag :location, :variable {:tag :variable, :name "u_Sampler", :type :sampler2D, :storage :uniform, :shader :texture-test}})

  (state-lookup d nil)

  (:init d)
  (:tag (example-shader))


  ;;;;

  ;; draw from texture1 to texture2
  ;; draw from texture2 onto screen
  ;;

  (defn init-framebuffer [fb tex rb width height]
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
        :type ::c/unsigned-byte :pixels nil})
     (gd/framebufferTexture2D
       {:framebuffer fb}
       {:attachment ::c/color-attachment0
        :texture    (:texture tex)})])







  (defn render-texture-texture [image]
    (let [t (gd/texture)
          tex {:texture t :texture-unit 0}

          texture-init (tex/texture-image
                         tex
                         {:target ::c/texture-2d
                          :format ::c/rgba
                          :type   ::c/unsigned-byte
                          :s      ::c/repeat
                          :t      ::c/repeat
                          :min    ::c/linear
                          :mag    ::c/linear
                          :data   image})

          fb1-tex (gd/texture)
          fb1-tex-set {:texture fb1-tex :texture-unit 1}
          fb1 (gd/framebuffer)
          rb1 (gd/renderbuffer)
          fb1-init (init-framebuffer fb1 fb1-tex-set rb1 512 512)

          a-position (g/attribute "a_Position" :vec2)
          a-tex-coord (g/attribute "a_TexCoord" :vec2)
          u-sampler (g/uniform "u_Sampler" :sampler2D)


          shader-draw1 (r/shader-draw
                         (assoc (texture-shader a-position a-tex-coord u-sampler) :id :s1)
                         {u-sampler tex} fb)
          shader-draw2 (r/shader-draw
                         (assoc (texture-shader a-position a-tex-coord u-sampler) :id :s2)
                         {u-sampler fb-tex-set} nil)

          commands
          [texture-init
           fb-init
           (:commands shader-draw1)
           (:commands shader-draw2)]

          texture-draw-params
          {:shader {a-position  (->float32 [[-1 1] [-1 -1] [1 1]
                                            [-1 -1] [1 1] [1 -1]])
                    a-tex-coord (->float32 [[0 1] [0 0] [1 1]
                                            [0 0] [1 1] [1 0]])}
           :draw   {:start 0 :count 6}}

          inputs
          (driver/assoc-inputs
            {:stage1 (:inputs shader-draw1)
             :stage2 (:inputs shader-draw2)}
            {:stage1 texture-draw-params
             :stage2 texture-draw-params})

          d (driver/driver
              commands
              {:gl (get-context "gl-canvas")})

          ]
      (driver/exec! d inputs)
      {:driver d
       :fb1    fb1}))


  (read-fb-pixels (state-lookup d fb) 1000)

  (defn read-fb-pixels [fb num]
    (let [pixels (js/Uint8Array. (* 512 512 4))]
      (.bindFramebuffer
        (state-lookup d :gl)
        (c/constants ::c/framebuffer)
        fb
        )
      (.readPixels
        (state-lookup d :gl)
        0
        0
        512
        512
        (c/constants ::c/rgba)
        (c/constants ::c/unsigned-byte)
        pixels)
      (for [x (range 5000)] (aget pixels x))))









  )



