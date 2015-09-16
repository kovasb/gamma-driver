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


(comment

  (def image (js/Image.))
  (aset image "onload" (fn [] (js/alert "image loaded")))
  (aset image "src" "nehe.gif")
  (def gl (get-context "gl-canvas"))

  (require '[gamma.webgl.api :as api])

  (let [a-position (g/attribute "a_Position" :vec2)
        a-tex-coord (g/attribute "a_TexCoord" :vec2)
        u-sampler (g/uniform "u_Sampler" :sampler2D)
        p (texture-shader a-position a-tex-coord u-sampler)

        gl (get-context "gl-canvas")
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



(comment



  (defn draw-texture [image gl]
   (let [model (root/root (atom {}) gl)
         tex (assoc (gamma.webgl.api/texture)
               :target :texture-2d
               :format :rgba
               :type :unsigned-byte)

         a-position (g/attribute "a_Position" :vec2)
         a-tex-coord (g/attribute "a_TexCoord" :vec2)
         u-sampler (g/uniform "u_Sampler" :sampler2D)

         s (texture-shader a-position a-tex-coord u-sampler)
         ab-pos (gamma.webgl.api/arraybuffer)
         ab-coord (gamma.webgl.api/arraybuffer)]
     (m/conform
       model
       {:arraybuffers {ab-pos   {:data [[-1 1] [-1 -1] [1 1]
                                        [-1 -1] [1 1] [1 -1]]}
                       ab-coord {:data [[0 1] [0 0] [1 1]
                                        [0 0] [1 1] [1 0]]}}})
     (m/conform
       model
       {:texture-units {0 {tex {:data image}}}})
     (m/conform
       model
       {:bindings {:program s}})
     (m/conform
       model
       {:texture-units {0 tex}
        :programs      {s {:attributes
                                     {a-position  {:arraybuffer ab-pos :layout (default-layout a-position)}
                                      a-tex-coord {:arraybuffer ab-coord :layout (default-layout a-tex-coord)}}
                           :uniforms {u-sampler 0}}}})

     (.drawArrays
       gl (c/constants :triangles) 0 6)
     model)))



(comment
  (require '[gamma.webgl.model.root :as root])
  (require '[gamma.webgl.model.core :as m])

  (def image (js/Image.))
  (aset image "onload" (fn [] (js/alert "image loaded")))
  (aset image "src" "nehe.gif")


  (def gl (get-context "gl-canvas"))



  (def r
    (draw-texture
     image
     gl))


  )



(comment
  (let [tex {:texture-unit 0 :texture (image-texture)}
        abs (attribute-buffers (:inputs s))]
    (textureData tex {:data image})
    (bufferData abs {:position [[-1 1] [-1 -1] [1 1]
                                [-1 -1] [1 1] [1 -1]]
                     :coordinates [[0 1] [0 0] [1 1]
                                   [0 0] [1 1] [1 0]]})
    (drawArrays {:program    s
                 :attributes (attribute-bindings attribute-buffers)
                 :uniforms   {:sampler tex}}))
  )

(comment
  (let [tex {:texture-unit 0 :texture (pixel-texture)}
        abs (attribute-buffers (:inputs s))
        fb (framebuffer w h tex)]

    (textureData tex {:data image})
    (bufferData abs {:position [[-1 1] [-1 -1] [1 1]
                                [-1 -1] [1 1] [1 -1]]
                     :coordinates [[0 1] [0 0] [1 1]
                                   [0 0] [1 1] [1 0]]})
    (drawArrays {:program    s
                 :framebuffer fb
                 :attributes (attribute-bindings attribute-buffers)})


    (drawArrays {:program s2
                 :framebuffer nil
                 :attributes x})
    )



  )









