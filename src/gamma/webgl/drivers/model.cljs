(ns gamma.webgl.drivers.model
  (:require [gamma.webgl.shader :as shader]
            [gamma.webgl.platform.constants :as c]))

(defprotocol IModel
  (conform [this val])
  (resolve [this val]))

(defn ->float32 [x]
  (js/Float32Array.
    (clj->js (flatten x))))

(defn resolve-in [x p]
  (reduce #(resolve %1 %2) x p))

(defn delegate [f parts v]
  (reduce-kv
    (fn [_ k v]
      (f (parts k) v))
    nil
    v))

(defrecord Root [parts gl]
  IModel
  (conform [this val]
    (delegate conform @parts val))
  (resolve [this val]
    (@parts val)))



(defn bind-attribute [gl location bo layout]
  (let [{:keys [size type normalized? stride offset]} layout]
    (.bindBuffer gl (.-ARRAY_BUFFER gl) bo)
    (.vertexAttribPointer
      gl
      location
      size
      type
      normalized?
      stride
      offset)
    (.enableVertexAttribArray gl location)))


(defn bind-attributes [root val locations]
  (reduce-kv
    (fn [_ k v]
      (bind-attribute
        (:gl root)
        (locations k)
        (resolve-in root [:arraybuffers (:arraybuffer v) :object])
        (:layout v)))
    nil
    val))

(defrecord Attributes [root parts variable-locations]
  IModel
  (resolve [this val]
    (@parts val))
  (conform [this val]
    (let [p @parts]
      (when (not= val p)
        (bind-attributes root val variable-locations)
        (reset! parts val)))))



(defrecord Program [root parts]
  IModel
  (resolve [this val]
    (@parts val))
  (conform [this val]
    (delegate conform @parts val)))




(defn variable-locations [gl shader obj]
  (reduce (fn [x y]
            (assoc
              x
              y
              (if (= :attribute (:storage y))
                (.getAttribLocation gl obj (:name y))
                (.getUniformLocation gl obj (:name y)))))
          {}
          (:inputs shader)))


(defn create-program [root val]
  (let [o (gamma.webgl.shader/install-shader (:gl root) val)
        variable-locations (variable-locations (:gl root) val o)]
    (Program.
     root
     (atom
       {:attributes (->Attributes root (atom {}) variable-locations)
        ;:uniforms   (->Uniforms root (atom {}) variable-locations)
       :object     o}))))


(defrecord Programs [root parts]
  IModel
  (resolve [this val]
    (let [p @parts]
      (if-let [x (p val)]
        x
        (let [x (create-program root val)]
          (swap! parts assoc val x)
          x))))
  (conform [this val]
    (delegate conform #(resolve this %) val)))


(defn arraybuffer-data [gl bo mode data]
  (.bindBuffer gl (.-ARRAY_BUFFER gl) bo)
  (.bufferData
    gl
    (.-ARRAY_BUFFER gl)
    data
    (.-STATIC_DRAW gl)))



(defrecord Arraybuffer [root parts compare bufferdata-fn]
  IModel
  (resolve [this val]
    (@parts val))
  (conform [this val]
    (reduce-kv
      (fn [_ k v]
        (let [p @parts]
          (case k
           :data (when (not (compare (:data p) v))
                   (arraybuffer-data (:gl root) (:object p) (:mode p) (bufferdata-fn v))
                   (swap! parts assoc :data v)))))
      nil
      val)))


(defn create-arraybuffer [root val]
  (let [o (.createBuffer (:gl root))]
    (->Arraybuffer root (atom (assoc val :object o)) = ->float32)))


(defrecord Arraybuffers [root parts]
  IModel
  (resolve [this val]
    (if-let [a (@parts val)]
      a
      (let [a (create-arraybuffer root val)]
        (swap! parts assoc val a)
        a)))
  (conform [this val]
    (delegate conform #(resolve this %) val)))



(defrecord GlobalBindings [root parts]
  IModel
  (resolve [this v]
    (@parts v))
  (conform [this val]
    (reduce-kv
      (fn [_ k v]
        (case k
          :program
          (when (not= v (resolve this k))
            (.useProgram
              (:gl root)
              (resolve-in root [:programs v :object]))
            (swap! parts assoc k v))))
      nil
      val)))


(defn root [a gl]
  (let [r (->Root a gl)]
    (swap! a merge {:arraybuffers (->Arraybuffers r (atom {}))
                    :bindings (->GlobalBindings r (atom {}))
                    ;:texture-units (texture-units r)
                    :programs (->Programs r (atom {}))})
    r))

(comment
  (def a (atom {}))

  (def r (->Root a nil))

  (swap! a merge {:arraybuffers (->Arraybuffers r (atom {}))
                  :bindings (->GlobalBindings r (atom {}))
                  ;:texture-units (texture-units r)
                  :programs (->Programs r (atom {}))})



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

  (gamma.webgl.shader/install-shader (:gl model) s)



  (resolve-in model [:arraybuffers ab :object])
  (resolve-in model [:arraybuffers ab :data])
  (resolve-in model [:programs s :object])

  (conform
    (resolve-in model [:arraybuffers ab])
    {:data x})


  (conform model {:bindings {:program s}})

  (resolve model :arraybuffers)

  ;; isn't creating the ab

  
  (do
    (conform model {:arraybuffers {ab {:data x}}})
    (conform
      model
      {:programs
       {s {:attributes {pos {:arraybuffer ab
                             :layout      (default-layout pos)}}}}})
    (conform model {:bindings {:program s}})
    (.drawArrays
      gl (c/constants ::c/triangles) 0 3)

    )



  )