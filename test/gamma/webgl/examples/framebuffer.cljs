(ns gamma.webgl.examples.framebuffer
  (:require
    [gamma.api :as g]
    [gamma.webgl.platform.constants :as c]
    [gamma.webgl.shader :as shader]
    [gamma.webgl.api :as api]
    [gamma.webgl.model.root :as root]
    [gamma.webgl.model.core :as m]))


(defn ->framebuffer [w h]
  (api/framebuffer
    {:attachments
     {:color-attachment0
      (api/texture-pixels
        {:format :rgba
         :type :unsigned-byte
         :width w
         :height h})
      :depth-attachment
      (api/renderbuffer
        {:width w
         :height h
         :internalformat :depth-component16})}}))

(comment



  (def fb (->framebuffer 512 512))
  (get-in fb [:attachments :color-attachment0])

  (def gl (get-context "gl-canvas"))
  (def model (root/root (atom {}) gl))

  (m/resolve-in model [:framebuffers fb :object])



  (defn draw1 []
    (reify IOp
      (exec! ))

    )


  )


(comment
  ;; draw to framebuffer

  (def pos (g/attribute "posAttr" :vec2))

  (defn triangle-shader []
    (shader/compile
      {:id              :hello-triangle
       :vertex-shader   {(g/gl-position) (g/vec4 pos 0 1)}
       :fragment-shader {(g/gl-frag-color) (g/vec4 1 0 0 1)}}))

  (defn draw-to-fb [model fb]
    (let [p (triangle-shader)
         ab (api/arraybuffer)
         bd (api/buffer-data model ab)
         draw (api/draw-arrays
                model
                {:program     p
                 :framebuffer fb
                 :attributes  {pos {:arraybuffer ab :layout (default-layout pos)}}})]
     (api/exec! bd {:data [[-1 -1] [0 1] [0 -1]]})
     (api/exec! draw {:start 0 :count 3 :mode :triangles})))

  (def fb (->framebuffer 512 512))

  (def gl (get-context "gl-canvas"))
  (def model (root/root (atom {}) gl))

  (def tex-data
    (api/texture-data
      model {:texture-unit 0

             :texture (get-in fb [:attachments :color-attachment0])}))
  (api/exec! tex-data {:data nil})
  (draw-to-fb model fb)

  ;; Sample from framebuffer


  (defn texture-shader [a-position a-tex-coord u-sampler]
    (let [v-tex-coord (g/varying "v_TexCoord" :vec2 :mediump)]
      (shader/compile
        {:id :texture-shader
         :vertex-shader
                          {(g/gl-position) (g/vec4 a-position 0 1)
                           v-tex-coord     a-tex-coord}
         :fragment-shader {(g/gl-frag-color) (g/texture2D u-sampler v-tex-coord)}})))



  (let [a-position (g/attribute "a_Position" :vec2)
        a-tex-coord (g/attribute "a_TexCoord" :vec2)
        u-sampler (g/uniform "u_Sampler" :sampler2D)
        p (texture-shader a-position a-tex-coord u-sampler)



        tex (get-in fb [:attachments :color-attachment0])

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
    ;(api/exec! tex-data {:data image})
    (api/exec! draw {:start 0 :count 6 :mode :triangles
                     :uniforms {u-sampler {:texture-unit 0 :texture tex}}}))





  )




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
  ;; composing programs

  (require '[gamma.webgl.examples.triangle :as examples.triangle])
  (require '[gamma.webgl.examples.texture :as examples.texture])

    (def gl (get-context "gl-canvas"))

    (def model (root/root (atom {}) gl))

  (let [fb (->framebuffer 512 512)

        tex {:texture-unit 0 :texture (get-in fb [:attachments :color-attachment0])}

        texture-init (api/texture-data model tex)
        texture-draw (examples.texture/texture-draw model nil tex)
        triangle-draw (examples.triangle/triangle-draw model fb)]
    (api/exec! texture-init {:data nil})

    (api/exec! triangle-draw {:data [[-0.5 -0.5] [0 1] [0 -1]]
                              :draw {:start 0 :count 3 :mode :triangles}})
    (api/exec! texture-draw {:draw {:start 0 :count 6 :mode :triangles}
                             :positions {:data [[-1 1] [-1 -1] [1 1]
                                                [-1 -1] [1 1] [1 -1]]}
                             :texture-coordinates {:data [[0 1] [0 0] [1 1]
                                                          [0 0] [1 1] [1 0]]}}))

  (def ops
    (let [fb (->framebuffer 512 512)

         tex {:texture-unit 0 :texture (get-in fb [:attachments :color-attachment0])}

         texture-init (api/texture-data model tex)
         texture-draw (examples.texture/texture-draw model nil tex)
         triangle-draw (examples.triangle/triangle-draw model fb)]
     {:texture-init texture-init :texture-draw texture-draw :triangle-draw triangle-draw}))

  (api/exec! (:texture-init ops) {:data nil})

  (api/exec! (:triangle-draw ops) {:data [[-0.5 -0.5] [0 1] [0 -1]]
                            :draw {:start 0 :count 3 :mode :triangles}})
  (api/exec! (:texture-draw ops) {:draw {:start 0 :count 6 :mode :triangles}
                           :positions {:data [[-1 1] [-1 -1] [1 1]
                                              [-1 -1] [1 1] [1 -1]]}
                           :texture-coordinates {:data [[0 1] [0 0] [1 1]
                                                        [0 0] [1 1] [1 0]]}})

  (m/resolve-in model [:bindings :program])



  (defn two-pass-draw [pass1 fb]
    (let [tex {:texture-unit 0 :texture (get-in fb [:attachments :color-attachment0])}
          texture-init (api/texture-data model tex)
          texture-draw (examples.texture/texture-draw model nil tex)]
      (reify
        api/IOp
        (exec! [this args]
          (api/exec! texture-init {:data nil})
          (api/exec! pass1 (:pass1 args))
          (api/exec! texture-draw (:pass2 args))))))

  (def two-pass-draw-op (let [fb (->framebuffer 512 512)]
                       (two-pass-draw
                        (examples.triangle/triangle-draw model fb)
                        fb)))

  (api/exec! two-pass-draw-op
             {:pass1 {:data [[-0.5 -1] [0 1] [0 -1]]
                      :draw {:start 0 :count 3 :mode :triangles}}
              :pass2 {:draw {:start 0 :count 6 :mode :triangles}
                      :positions {:data [[-1 1] [-1 -1] [1 1]
                                         [-1 -1] [1 1] [1 -1]]}
                      :texture-coordinates {:data [[0 1] [0 0] [1 1]
                                                   [0 0] [1 1] [1 0]]}}
              })





  )
