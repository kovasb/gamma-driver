(ns gamma.webgl.compiler.core
  (:require
    [goog.webgl :as ggl]
    [clojure.walk]
    [gamma.webgl.api :as gd]
    [gamma.webgl.compiler.attribute :as attr]
    [gamma.webgl.compiler.arraybuffer :as ab]
    [gamma.webgl.compiler.framebuffer :as fb]
    [gamma.webgl.compiler.uniform :as uniform]
    [gamma.webgl.compiler.draw :as draw]
    [gamma.webgl.shader :as shader]
    [gamma.webgl.platform.constants :as c]))


(def loop-rules
  {::gd/bind-attribute
   (fn [t o]
     (attr/bind-attribute
       o
       (attr/default-layout t)
       {:tag :location :variable t}))

   ::gd/bind-uniform
   (fn [t o]
     (uniform/uniform-input (:type t) {:tag :location :variable t} o))

   ::gd/bind-arraybuffer
   (fn [t o] (ab/arraybuffer-input t o))

   ::gd/bind-framebuffer
   (fn [x]
     (fb/bind-fb nil))

   ::gd/current-shader
   (fn [x] [:useProgram :gl x])

   ::gd/draw-arrays
   (fn [y z] (draw/draw-arrays ::c/triangles y z))

   })

(defn rewrite-fn [x]
  (if (not (vector? x))
    x
    (if-let [rw (loop-rules (first x))]
      (apply rw (rest x))
      x)))

(defn compile-loop [ops]
  (clojure.walk/postwalk
    rewrite-fn
    ops))


(def init-rules
  {::gd/arraybuffer ab/create-array-buffer
   ::gd/shader shader/init-shader})


(defn compile-init [ops]
  (keep
    (fn [x]
      (if-let [r (init-rules (:tag x))]
        (r x)))
    (set (filter map? (flatten ops)))))


(defn compile [ops]
  {:init (compile-init ops)
   :loop (compile-loop ops)})



(comment
  ;; init of compound structures
  ;; can build a dag based on contained elements
  ;; what about relational structures?

  )
