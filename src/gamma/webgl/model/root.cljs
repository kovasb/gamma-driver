(ns gamma.webgl.model.root
  (:require [gamma.webgl.model.core :as m]
            [gamma.webgl.model.program :as program]
            [gamma.webgl.model.arraybuffers :as arraybuffers]
            [gamma.webgl.model.programs :as programs]
            [gamma.webgl.model.bindings :as bindings]
            [gamma.webgl.model.textures :as textures]
            [gamma.webgl.model.uniforms :as uniforms]
            [gamma.webgl.model.framebuffers :as framebuffers]
            [gamma.webgl.model.renderbuffers :as renderbuffers]
            [gamma.webgl.platform.constants :as c]))


(extend-protocol
  IPrintWithWriter
  Atom
  (-pr-writer [a writer opts]
    (-write writer "#object [cljs.core.Atom ")
    (-write writer "]")))


(defrecord Root [parts gl]
  m/IModel
  (conform [this val]
    (m/delegate m/conform @parts val))
  (resolve [this val]
    (@parts val)))

(defn root [a gl]
  (let [r (->Root a gl)]
    (swap! a merge {:arraybuffers (arraybuffers/->Arraybuffers r (atom {}))
                    :framebuffers (framebuffers/->Framebuffers r (atom {}))
                    :renderbuffers (renderbuffers/->Renderbuffers r (atom {}))
                    :bindings (bindings/->GlobalBindings r (atom {}))
                    :texture-units (textures/->TextureUnits r (atom {}))
                    :textures (textures/->Textures r (atom {}) textures/create-texture)
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
     :type        (c/constants :float)
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


  (drawArrays
    model
    {:program    x :framebuffer {:tag :fb
                                 }
     :attributes x
     :uniforms   {:sampler {:texture-unit 0 :texture t}}}
    {:first x :count y :mode z})


  (conform!
    (resolve-in model [:programs p :uniforms])
    data)

  (conform
    model
    {:arraybuffers {ab {:data x}}})


  (do
    (m/conform model {:arraybuffers {ab {:data x}}})
    (m/conform
      model
      {:programs
       {s {:attributes {pos {:arraybuffer ab
                             :layout      (default-layout pos)}}}}})
    (m/conform model {:bindings {:program s}})
    (.drawArrays
      gl (c/constants :triangles) 0 3))


  ;; simple uniform

  (def pos (g/attribute "posAttr" :vec2))
  (def color (g/uniform "colorAttr" :vec4 :mediump))


  (defn example-shader []
    (shader/compile
      {:id              :hello-triangle
       :vertex-shader   {(g/gl-position) (g/vec4 pos 0 1)}
       :fragment-shader {(g/gl-frag-color) color}
       :precision {:float :mediump}}))

  (def s (example-shader))

  (def model (root (atom {}) gl))


  (do
    (m/conform model {:bindings {:program s}})
    ;; should this happen automatically if we try to change uniforms?
    (m/conform model {:arraybuffers {ab {:data x}}})
    (m/conform
      model
      {:programs
       {s {:uniforms {color [0 1 0 1]}
           :attributes {pos {:arraybuffer ab
                             :layout      (default-layout pos)}}}}})
    (m/conform model {:bindings {:program s}})
    (.drawArrays
      gl (c/constants :triangles) 0 3))

  (.gl :foo 1)


  ;; Texture Uniform

















  (textureData {0 tex} data)
  ;; impl

  (conform model {:texture-units {0 {tex {:data x}}}})


  (drawArrays {:program 1 :uniforms {sampler {:texture-unit 0 :texture t}}})
  ;; possible conflict if
  (drawArrays {:program 2 :uniforms {sampler {:texture-unit 0 :texture t2}}})

  (drawArrays {:program 2
               :uniforms x
               :texture-uniforms {sampler {:texture-unit 0}}
               :texture-units {0 tex}})


  (textureData {:texture-units {0 {tex {:data d}}}})


  ;; need to specify all texture units??
  ;; situation is analogous to arraybuffers

  (defn bind-texture-uniform [location id]
    [[:activeTexture :gl id]
     [:uniformli :gl location id]])
  ;; can we just specify uniformli without doing activeTexture?




  ;; if texture unit has changed, need to rebind uniform. uniform+unit
  ;; if texture bound to texture  unit has changed, need to rebind unit+tex


  :textures {tex {:object x :format-type x :pixels/image z}}

  ;; modifying something in textures requires
  ;; setting active texture unit
  ;; setting texture-units

  :texture-units {0 tex}

  :bindings {:active-texture {:texture-unit 0 :texture tex}}

  ;; tex ops
  ;; set data
  ;; bind to uniform


  {:tag :framebuffer :color0 {:texture x y z}}

  (textureData {:texture tex :texture-unit 0} data)

  (drawArrays
    :framebuffer X)

  ;; is possible to bind texture to fb without active texture?
  ;; appears to be the case
  ;; but need to test



  (conform tu {0 {tex {:pixels y}}})
  (conform framebuffers {fb fb})

  :texture-uniforms {sampler {:tex-unit 0 :tex tex}}




  )