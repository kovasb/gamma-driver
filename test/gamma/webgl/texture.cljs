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
                :id       0}
   })


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

  (def shader-draw (r/shader-draw (example-shader) {u-sampler tex}))


  (def d (driver/driver
           (concat texture-init (:commands shader-draw))
           {:gl (get-context "gl-canvas")}))

  (driver/exec!
    d
    (driver/assoc-inputs
      (:inputs shader-draw)
      {:shader {a-position  (->float32 [[-0.5 0.5] [-0.5 -0.5] [0.5 0.5]
                                        [-0.5 -0.5] [0.5 0.5] [0.5 -0.5]])
                a-tex-coord (->float32 [[0 1] [0 0] [1 1]
                              [0 0] [1 1] [1 0]])}
       :draw   {:start 0 :count 6}}))

  (defn state-lookup [driver val]
    (@(:state (:interpreter driver)) val))

  (def v {:tag :location, :variable {:tag :variable, :name "u_Sampler", :type :sampler2D, :storage :uniform, :shader :texture-test}})

  (state-lookup d nil)

  (:init d)
  (:tag (example-shader))





  )



(defn example-driver []
  (let [c (.getContext (.getElementById js/document "gl-canvas") "webgl")]
    (.enable c (.-DEPTH_TEST c))
    (.clear c (.-DEPTH_BUFFER_BIT c))
    (driver/basic-driver c)))

(comment


  )
(defn main []
  (let [image (js/Image.)
        d (example-driver)
        p (example-program)]
    (aset image "onload"
          (fn [] (gd/draw-arrays
                   d
                   (gd/bind d p (example-data image))
                   {})))
    (aset image "src" "nehe.png")))