(ns gamma.webgl.shader
  (:require [gamma.webgl.api :as api]
            [goog.webgl :as ggl]
    ;[gamma-driver.impl.resource :as resource]
            [gamma.webgl.compiler.uniform :as uniform]
            [gamma.webgl.compiler.attribute :as attribute]
            [gamma.program :as p]))



(deftype Shader [value]
  IWithMeta
  (-with-meta [_ new-meta]
    (Shader. (with-meta value new-meta)))
  IMeta
  (-meta [_] (meta value))
  ICounted
  (-count [_]
    (-count value))
  ICollection
  (-conj [_ o]
    (Shader. (-conj value o)))
  ;; EXPERIMENTAL
  IEmptyableCollection
  (-empty [_]
    (Shader. (empty value)))
  ILookup
  (-lookup [this k]
    (-lookup this k nil))
  (-lookup [this k not-found]
    (-lookup value k not-found))
  IFn
  (-invoke [this k]
    (-lookup this k))
  (-invoke [this k not-found]
    (-lookup this k not-found))
  ISeqable
  (-seq [this]
    (-seq value))
  IAssociative
  (-contains-key? [_ k]
    (-contains-key? value k))
  (-assoc [_ k v]
    (Shader. (-assoc value k v)))
  IMap
  (-dissoc [_ k]
    (Shader. (-dissoc value k)))
  IEquiv
  (-equiv [_ other]
    (= value other))
  IHash
  (-hash [_]
    (hash value))
  IKVReduce
  (-kv-reduce [_ f init]
    (-kv-reduce value f init))
  IPrintWithWriter
  (-pr-writer [_ writer opts]
    (-write writer (str "#<Shader " (:id value) ">"))))



(defn install-shader* [gl spec]
  (let [s (.createShader
            gl
            ({:vertex-shader ggl/VERTEX_SHADER
              :fragment-shader ggl/FRAGMENT_SHADER}
              (:tag spec)))]
    (if s
      (do
        (.shaderSource gl s (:glsl spec))
        (.compileShader gl s)
        (let [compiled (.getShaderParameter gl s ggl/COMPILE_STATUS)]
          (if compiled
            (assoc spec (:tag spec) s)
            (throw (js/Error. (str "failed to compile " (name (:tag spec)) ":"
                                   (.getShaderInfoLog gl s)))))))
      (throw (js/Error. (str "Unable to create " (name (:tag spec))))))))


(defn install-shader [gl spec]
  (let [v (install-shader* gl (assoc (:vertex-shader spec) :tag :vertex-shader))
        f (install-shader* gl (assoc (:fragment-shader spec) :tag :fragment-shader))
        p (.createProgram gl)]
    (.attachShader gl p (:vertex-shader v))
    (.attachShader gl p (:fragment-shader f))
    (.linkProgram gl p)
    (if (.getProgramParameter gl p ggl/LINK_STATUS)
      (do
        (.useProgram gl p)
        p)
      (throw
        (js/Error.
          (str "failed to link program: "
               (.getProgramInfoLog gl p)))))))


(defn init-variable-location [shader variable]
  {:op :assign
   :args
   [{:tag :location :variable (assoc variable :shader (:id shader))}
    {:op (if (= :attribute (:storage variable))
           :getAttribLocation
           :getUniformLocation)
     :args
     [:gl shader (:name variable)]}]})

(comment
  [:assign
   {:tag :location :variable (assoc variable :shader (:id shader))}
   [(if (= :attribute (:storage variable))
      :getAttribLocation
      :getUniformLocation)
    :gl
    shader
    (:name variable)]])


(defn init-shader [shader]
  [{:op :assign
    :args [shader
           {:op :createShader
            :args [:gl {:op :value :args [{:tag :literal :value shader}]}]}]}
   (map #(init-variable-location shader %)
        (:inputs shader))])


(comment
  [[:assign
    shader
    [:createShader :gl [:value {:tag :literal :value shader}]]]
   ])

(defn compile [x]
  (assoc
    (Shader.
      (p/program x))
    :tag :gamma.webgl.api/shader))





