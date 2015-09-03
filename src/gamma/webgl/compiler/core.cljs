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




(comment
  (defn op [op bindings & args]))


(defn create-array-buffer [ab]
  {:op :assign
   :args [ab {:op :createBuffer :args [:gl]}]})

(defn create-texture [t]
  {:op :assign
   :args [t {:op :createTexture :args [:gl]}]})

(defn create-framebuffer [x]
  {:op :assign
   :args [x {:op :createFramebuffer :args [:gl]}]})

(defn create-renderbuffer [x]
  {:op :assign
   :args [x {:op :createRenderbuffer :args [:gl]}]})


(defn create-extension [x]
  {:op :assign
   :args [x {:op :getExtension :args [:gl (:extension x)]}]})

(def init-rules
  {:arraybuffer create-array-buffer
   :element-arraybuffer create-array-buffer
   :framebuffer create-framebuffer
   :renderbuffer create-renderbuffer
   :texture create-texture
   :extension create-extension
   ::gd/shader shader/init-shader})

(defn collect-tagged [x]
  (let [a (atom [])]
    (clojure.walk/prewalk
     (fn [y]
       (if (and (map? y) (:tag y))
         (if (= ::gd/shader (:tag y))
           (do (swap! a conj y) nil)
           (do (swap! a conj y) y))
         y))
     x)
    @a))


(defn compile-init [ops]
  (keep
    (fn [x]
      (if-let [r (init-rules (:tag x))]
        (r x)))
    (set (collect-tagged ops))))




(comment
  ;; init of compound structures
  ;; can build a dag based on contained elements
  ;; what about relational structures?



  )
