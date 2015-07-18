(ns gamma.webgl.operations
  (:require
    [goog.webgl :as ggl]
    [clojure.walk]
    [gamma.webgl.attribute :as attr]
    [gamma.webgl.arraybuffer :as ab]
    [gamma.webgl.framebuffer :as fb]
    [gamma.webgl.draw :as draw]
    [gamma.webgl.shader :as shader]))


;;;;;;;;;;;;;;;;;;;;;;;;;


(def rewrites
  {:bind-attribute
   (fn [t o]
     (attr/bind-attribute
       o
       (attr/default-layout t)
       {:tag :location :variable t}))

   :bind-arraybuffer
   (fn [t o] (ab/arraybuffer-input t o))

   :bind-framebuffer
   (fn [x]
     (fb/bind-fb nil))

   :current-shader
   (fn [x] [:useProgram :gl x])

   :draw-arrays
   (fn [y z] (draw/draw-arrays ggl/TRIANGLES y z))

   })

(defn rewrite-fn [x]
  (if (not (vector? x))
    x
    (if-let [rw (rewrites (first x))]
      (apply rw (rest x))
      x)))

(defn rewrite [x]
  (clojure.walk/postwalk
    rewrite-fn
    x))



(def init-rules
  {:arraybuffer ab/create-array-buffer
   :shader shader/init-shader})


(defn initialization [ops]
  (keep
    (fn [x]
      (if-let [r (init-rules (:tag x))]
        (r x)))
    (set (filter map? (flatten ops)))))



