(ns gamma.webgl.examples.framebuffer
  (:require
    [gamma.api :as g]
    [gamma.webgl.platform.constants :as c]
    [gamma.webgl.shader :as shader]
    [gamma.webgl.api]
    [gamma.webgl.model.root :as root]
    [gamma.webgl.model.core :as m]))



(comment
  (resolve
    model
    {:framebuffers {fb}})

  {:framebuffers
   {fb
    {:object fb-object
     :attachments
     {:color0 tex-spec
      :depth renderbuffer-spec}}}
   :renderbuffers
   {rb {:object x ...}}}

  (create-framebuffer root
    {:tag :framebuffer
     :attachments
     {:color0 {:tag :texture
               :format x
               :type x
               :width x
               :height x}
      :depth {:tag :renderbuffer
              :width x
              :height x}}}))


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


(defn ->framebuffer [w h]
  (api/framebuffer
    {:attachments
     {:color-attachment0
      (texture-pixels
        {:format :rgba
         :type :unsigned-byte
         :width w
         :height h})
      :depth-attachment
      (renderbuffer
        {:width w
         :height h
         :internalformat :rgba})}}))

(defn draw-framebuffer [image gl]
  (let [model (root/root (atom {}) gl)
        fb (->framebuffer 512 512)
        tex (get-in fb [:attachments :color-attachment0])


        a-position (g/attribute "a_Position" :vec2)
        a-tex-coord (g/attribute "a_TexCoord" :vec2)
        u-sampler (g/uniform "u_Sampler" :sampler2D)

        s (texture-shader a-position a-tex-coord u-sampler)
        ab-pos (gamma.webgl.api/arraybuffer)
        ab-coord (gamma.webgl.api/arraybuffer)

        fb (framebuffer
             {:attachments
              {:color-attachment0 tex
               :depth-attachment (renderbuffer
                                   {:width w :height h})}})

        ]
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
    model))
