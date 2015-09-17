(ns gamma.webgl.examples.texture
  (:require
    [gamma.api :as g]
    [gamma.webgl.platform.constants :as c]
    [gamma.webgl.shader :as shader]
    [gamma.webgl.api :as api]
    [gamma.webgl.model.root :as root]
    [gamma.webgl.model.core :as m]))


(defn default-layout [attribute]
  {:normalized? false
   :size        ({:float 1 :vec2 2 :vec3 3 :vec4 4}
                  (:type attribute))
   :type        (c/constants :float)
   :offset      0
   :stride      0})

(defn get-context [id]
  (.getContext
    (.getElementById js/document id)
    "webgl"))

(defn texture-shader [a-position a-tex-coord u-sampler]
  (let [v-tex-coord (g/varying "v_TexCoord" :vec2 :mediump)]
    (shader/compile
      {:id :texture-shader
       :vertex-shader
                        {(g/gl-position) (g/vec4 a-position 0 1)
                         v-tex-coord     a-tex-coord}
       :fragment-shader {(g/gl-frag-color) (g/texture2D u-sampler v-tex-coord)}})))

(defn texture-draw [model fb texture-pair]
  (let [a-position (g/attribute "a_Position" :vec2)
        a-tex-coord (g/attribute "a_TexCoord" :vec2)
        u-sampler (g/uniform "u_Sampler" :sampler2D)
        p (texture-shader a-position a-tex-coord u-sampler)

        ab-pos (api/arraybuffer)
        ab-coord (api/arraybuffer)

        bd-pos (api/buffer-data model ab-pos)
        bd-coord (api/buffer-data model ab-coord)

        draw (api/draw-arrays
               model
               {:program     p
                :framebuffer fb
                :attributes  {a-position  {:arraybuffer ab-pos :layout (default-layout a-position)}
                              a-tex-coord {:arraybuffer ab-coord :layout (default-layout a-tex-coord)}}})]
    (reify
      api/IOp
      (exec! [this args]
        (api/exec! bd-pos (:positions args))
        (api/exec! bd-coord (:texture-coordinates args))
        (api/exec! draw (assoc (:draw args) :uniforms {u-sampler texture-pair}))))))


(comment
  (def image (js/Image.))
  (aset image "onload" (fn [] (js/alert "image loaded")))
  (aset image "src" "nehe.gif")

  (def gl (get-context "gl-canvas"))
  (def model (root/root (atom {}) gl))

  (let [tex {:texture-unit 0
             :texture (api/texture-image {:format :rgba :type :unsigned-byte})}
        texture-init (api/texture-data model tex)
        texture-draw (texture-draw model nil tex)]
    (api/exec! texture-init {:data image})
    (api/exec! texture-draw {:draw {:start 0 :count 6 :mode :triangles}
                             :positions {:data [[-1 1] [-1 -1] [1 1]
                                                [-1 -1] [1 1] [1 -1]]}
                             :texture-coordinates {:data [[0 1] [0 0] [1 1]
                                                          [0 0] [1 1] [1 0]]}
                             }))


  )


(comment

  (def image (js/Image.))
  (aset image "onload" (fn [] (js/alert "image loaded")))
  (aset image "src" "nehe.gif")
  (def gl (get-context "gl-canvas"))


  (let [a-position (g/attribute "a_Position" :vec2)
        a-tex-coord (g/attribute "a_TexCoord" :vec2)
        u-sampler (g/uniform "u_Sampler" :sampler2D)
        p (texture-shader a-position a-tex-coord u-sampler)

        model (root/root (atom {}) gl)

        tex (api/texture-image {:format :rgba :type :unsigned-byte})

        ab-pos (api/arraybuffer)
        ab-coord (api/arraybuffer)

        bd-pos (api/buffer-data model ab-pos)
        bd-coord (api/buffer-data model ab-coord)
        tex-data (api/texture-data model {:texture-unit 0 :texture tex})
        draw (api/draw-arrays
               model
               {:program     p
                :framebuffer nil
                :attributes  {a-position  {:arraybuffer ab-pos :layout (default-layout a-position)}
                              a-tex-coord {:arraybuffer ab-coord :layout (default-layout a-tex-coord)}}})]
    (api/exec! bd-pos {:data [[-1 1] [-1 -1] [1 1]
                              [-1 -1] [1 1] [1 -1]]})
    (api/exec! bd-coord {:data [[0 1] [0 0] [1 1]
                                [0 0] [1 1] [1 0]]})
    (api/exec! tex-data {:data image})
    (api/exec! draw {:start 0 :count 6 :mode :triangles
                     :uniforms {u-sampler {:texture-unit 0 :texture tex}}}))

  )

























