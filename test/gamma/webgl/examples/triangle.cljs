(ns gamma.webgl.examples.triangle
  (:require
    [gamma.api :as g]
    [gamma.webgl.platform.constants :as c]
    [gamma.webgl.shader :as shader]
    [gamma.webgl.api :as api]
    [gamma.webgl.model.root :as root]
    [gamma.webgl.model.core :as m]))



(comment
  (let [p (example-shader)
       model ()
       bd (buffer-data model ab)
       draw (draw-arrays model
                         {:program     p
                          :framebuffer nil
                          :attributes  {attr {:buffer ab :layout (default-layout attr)}}})]
   (exec! bd data)
   (exec! draw {:start 0 :count 3})))


(comment
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

  (def s (example-shader))


  (defn default-layout [attribute]
    {:normalized? false
     :size        ({:float 1 :vec2 2 :vec3 3 :vec4 4}
                    (:type attribute))
     :type        (c/constants :float)
     :offset      0
     :stride      0})

  (def gl (get-context "gl-canvas"))
  (def model (root/root (atom {}) gl))
  (def ab (api/arraybuffer))

  (def bd (api/buffer-data model ab))

  (def draw
    (api/draw-arrays model
                 {:program s
                  :framebuffer nil
                  :attributes {pos {:arraybuffer ab :layout (default-layout pos)}}}))

  (api/exec! bd {:data [[0 0] [0 1] [0 -1]]})
  (api/exec! draw {:start 0 :count 3 :mode :triangles})

  (m/resolve-in model [:programs s])

  (println (:glsl (:vertex-shader s)))


  (m/conform
    model
    {
     :programs      {s {:attributes
                                  {pos  {:arraybuffer ab :layout (default-layout pos)}
                                   }
                        }}})

  (do
    (.useProgram gl (m/resolve-in model [:programs s :object]))
    (.bindBuffer gl (c/constants :array-buffer) (m/resolve-in model [:arraybuffers ab :object]))
    (.bufferData gl (c/constants :array-buffer) (js/Float32Array. #js [0 0 0 1 0 -1]) (c/constants :static-draw))
    (.drawArrays gl (c/constants :triangles) 0 3)
    )







  )