(ns gamma.webgl.interpreter
  (:require [gamma.webgl.instructions.core :as i]
            [gamma.webgl.shader]))

(defprotocol IEval (-eval [this instruction]))

(defrecord Interpreter [state]
  IEval
  (-eval [this x]
    (cond
      (and (sequential? x) (sequential? (first x)))
      (into [] (map #(-eval this %)) x)

      (sequential? x)
      (let [y (into [] (map #(-eval this %)) x)]
        (apply (first y) (rest y)))

      :default
      (if-let [y (@state x)] y x))))


(defn intrinsics []
  {:bindBuffer i/bindBuffer
   :bufferData i/bufferData
   :createBuffer i/createBuffer
   :vertexAttribPointer i/vertexAttribPointer
   :enableVertexAttribArray i/enableVertexAttribArray
   :getAttribLocation i/getAttribLocation
   :drawArrays i/drawArrays
   :drawElements i/drawElements
   :createShader gamma.webgl.shader/install-shader
   :bindFramebuffer i/bindFramebuffer})

(defn interpreter [init]
  (let [s (atom (merge (intrinsics) init))]
    (swap! s assoc
           :assign (fn [k v] (swap! s assoc k v))
           :get-in (fn [x y] (get-in (x) (:path y)))
           :env (fn [] @s))
    (Interpreter. s)))


(defn eval* [init instructions]
  (let [s (atom init)
        i (Interpreter. s)]
    (swap! s assoc
           :assign (fn [k v] (swap! s assoc k v))
           )
    (-eval i instructions)))






(comment
  (let [env (merge
              (instrinsics)
              {{:tag :program :program 1}
               program-source})])

  (eval* {:a vector} [[:assign :b 1] :b])

  {:tag :data-path :data-path [:a :b :c :d ]}

  )

