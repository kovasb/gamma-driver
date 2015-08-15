(ns gamma.webgl.interpreter
  (:require [gamma.webgl.platform.core :as i]
            [gamma.webgl.shader]
            [gamma.webgl.platform.constants :as c]))

(defprotocol IEval (-eval [this instruction]))

(defrecord Interpreter [state]
  IEval
  (-eval [this x]
    (try
      (cond
       (and (sequential? x) (sequential? (first x)))
       (into [] (map #(-eval this %)) x)

       (sequential? x)
       (let [y (into [] (map #(-eval this %)) x)]
         (if-let [f (first y)]
           (if (keyword? f)
             (if-let [m (aget (@state :gl) (name f))]
               ;(.apply (aget gl "uniform3fv") gl #js [l #js [0 1 0]])
               (.apply m (@state :gl) (clj->js (vec (drop 2 y))))
               (println ("no gl method: " (name f))))
             (apply (first y) (rest y)))
           (println "no fn in invocation")))

       :default
       (if-let [y (@state x)] y x))
      (catch js/Error e (println [x (.toString e)])))))


(defn intrinsics []
  {:bindBuffer              i/bindBuffer
   :bufferData              i/bufferData
   :createBuffer            i/createBuffer
   :vertexAttribPointer     i/vertexAttribPointer
   :enableVertexAttribArray i/enableVertexAttribArray
   :getAttribLocation       i/getAttribLocation
   :getUniformLocation      i/getUniformLocation
   :drawArrays              i/drawArrays
   :drawElements            i/drawElements
   :createShader            gamma.webgl.shader/install-shader
   :bindFramebuffer         i/bindFramebuffer})

(defn interpreter [init]
  (let [s (atom (merge (intrinsics) c/constants init))]
    (swap! s assoc
           :value (fn [x] (:value x))
           :assign (fn [k v] (swap! s assoc k v))
           :get-in (fn [x y] (let [r (get-in (x) (:path y))]
                               ;(println [(:path y) r])
                                r))
           :env (fn [] @s))
    (Interpreter. s)))

(comment
  [:get-in :env {:tag :path :path [:root :draw :count]}])

(defn eval* [init instructions]
  (let [s (atom init)
        i (Interpreter. s)]
    (swap! s assoc
           :assign (fn [k v] (swap! s assoc k v))
           )
    (-eval i instructions)))



