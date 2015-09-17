(ns gamma.webgl.examples.triangle
  (:require
    [gamma.api :as g]
    [gamma.webgl.platform.constants :as c]
    [gamma.webgl.shader :as shader]
    [gamma.webgl.api :as api]
    [gamma.webgl.model.root :as root]
    [gamma.webgl.model.core :as m]))


(defn example-shader [pos]
  (shader/compile
    {:id              :hello-triangle
     :vertex-shader   {(g/gl-position) (g/vec4 pos 0 1)}
     :fragment-shader {(g/gl-frag-color) (g/vec4 1 0 0 1)}}))

;; Helpers
(defn get-context [id]
  (.getContext
    (.getElementById js/document id)
    "webgl"))


(defn default-layout [attribute]
  {:normalized? false
   :size        ({:float 1 :vec2 2 :vec3 3 :vec4 4}
                  (:type attribute))
   :type        (c/constants :float)
   :offset      0
   :stride      0})


(defn triangle-draw [model fb]
  (let [pos (g/attribute "posAttr" :vec2)
        p (example-shader pos)
        ab (api/arraybuffer)
        bd (api/buffer-data model ab)
        draw (api/draw-arrays
               model
               {:program     p
                :framebuffer fb
                :attributes  {pos {:arraybuffer ab :layout (default-layout pos)}}})]
    (reify
      api/IOp
      (exec! [this args]
        (api/exec! bd {:data (:data args)})
        (api/exec! draw (:draw args))))))

(comment
  (def gl (get-context "gl-canvas"))
  (def model (root/root (atom {}) gl))
  (def td (triangle-draw model nil))
  (api/exec! td
             {:data [[-1 -1] [0 1] [0 -1]]
              :draw {:start 0 :count 3 :mode :triangles}})
  )

(comment
  (let [gl (get-context "gl-canvas")
        pos (g/attribute "posAttr" :vec2)
        p (example-shader pos)
        ab (api/arraybuffer)
        model (root/root (atom {}) gl)
        bd (api/buffer-data model ab)
        draw (api/draw-arrays
               model
               {:program     p
                :framebuffer nil
                :attributes  {pos {:arraybuffer ab :layout (default-layout pos)}}})]
    (api/exec! bd {:data [[-1 -1] [0 1] [0 -1]]})
    (api/exec! draw {:start 0 :count 3 :mode :triangles}))







  )


(comment
  ;; UNIFORMS

  (def pos (g/attribute "posAttr" :vec2))
  (def color (g/uniform "u_Color" :vec4 :mediump))

  (defn example-shader []
    (shader/compile
      {:id              :hello-triangle
       :vertex-shader   {(g/gl-position) (g/vec4 pos 0 1)}
       :fragment-shader {(g/gl-frag-color) color}
       :precision {:float :mediump}}))

  (def s (example-shader))

  (let [gl (get-context "gl-canvas")
        p (example-shader)
        ab (api/arraybuffer)
        model (root/root (atom {}) gl)
        bd (api/buffer-data model ab)
        draw (api/draw-arrays
               model
               {:program     p
                :framebuffer nil
                :attributes  {pos {:arraybuffer ab :layout (default-layout pos)}}})]
    (api/exec! bd {:data [[-1 -1] [0 1] [0 -1]]})
    (api/exec! draw {:start 0 :count 3 :mode :triangles
                     :uniforms {color #js [0 1 0 1]}}))






  )