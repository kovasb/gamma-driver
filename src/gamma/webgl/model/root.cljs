(ns gamma.webgl.model.root
  (:require [gamma.webgl.model.core :as m]
            [gamma.webgl.model.program :as program]
            [gamma.webgl.model.arraybuffers :as arraybuffers]
            [gamma.webgl.model.programs :as programs]
            [gamma.webgl.model.bindings :as bindings]
            [gamma.webgl.platform.constants :as c]))



(defrecord Root [parts gl]
  m/IModel
  (conform [this val]
    (m/delegate m/conform @parts val))
  (resolve [this val]
    (@parts val)))

(defn root [a gl]
  (let [r (->Root a gl)]
    (swap! a merge {:arraybuffers (arraybuffers/->Arraybuffers r (atom {}))
                    :bindings (bindings/->GlobalBindings r (atom {}))
                    ;:texture-units (texture-units r)
                    :programs (programs/->Programs r (atom {}))})
    r))


(comment
  (require 'gamma.webgl.model.root :reload)


  )

(comment

  (extend-protocol
    IPrintWithWriter
    Atom
    (-pr-writer [a writer opts]
      (-write writer "#object [cljs.core.Atom ")
      (-write writer "]")))

  (atom nil)


  (require '[gamma.api :as g])
  (require '[gamma.webgl.platform.constants :as c])
  (require '[gamma.webgl.shader :as shader])

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
     :type        (c/constants ::c/float)
     :offset      0
     :stride      0})


  (require 'gamma.webgl.api)

  (def ab (gamma.webgl.api/arraybuffer))
  (def x [[0 0] [0 1] [-1 1]])

  (def gl (get-context "gl-canvas"))
  (def model (root (atom {}) gl))

  @(:parts model)


  (gamma.webgl.shader/install-shader (:gl model) s)

  (m/resolve-in model [:arraybuffers ab])


  (m/resolve-in model [:arraybuffers ab :object])
  (m/resolve-in model [:arraybuffers ab :data])
  (m/resolve-in model [:programs s :object])

  (conform
    (resolve-in model [:arraybuffers ab])
    {:data x})


  (conform model {:bindings {:program s}})

  (resolve model :arraybuffers)

  ;; isn't creating the ab


  (do
    (m/conform model {:arraybuffers {ab {:data x}}})
    (m/conform
      model
      {:programs
       {s {:attributes {pos {:arraybuffer ab
                             :layout      (default-layout pos)}}}}})
    (m/conform model {:bindings {:program s}})
    (.drawArrays
      gl (c/constants ::c/triangles) 0 3)

    )

  



  )