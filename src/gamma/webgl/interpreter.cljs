(ns gamma.webgl.interpreter
  (:require [gamma.webgl.platform.core :as i]
            [gamma.webgl.shader]
            [gamma.webgl.platform.constants :as c]))

(defprotocol IEval (-eval [this instruction]))

(defn eval-binding [gl name spec val]
  (case name
    :texture (.bindTexture gl (c/constants (:target spec)) val)
    :texture-unit (.activeTexture gl (+ spec (c/constants ::c/texture0)))
    :arraybuffer (.bindBuffer gl (c/constants ::c/array-buffer) val)
    :element-arraybuffer (.bindBuffer gl (c/constants ::c/element-array-buffer) val)
    :framebuffer (.bindFramebuffer gl (c/constants ::c/framebuffer) val)
    :renderbuffer (.bindRenderbuffer gl (c/constants ::c/renderbuffer) val)
    :program (.useProgram gl val)
    (throw (js/Error (str "No binding operation matched for: " name)))))


(defn eval-bindings [gl state b]
  (reduce
    (fn [_ [k v]] (eval-binding gl k v (state v)))
    nil
    (sort-by
      first
      (fn [a b]
        (cond
          (and (= a :texture) (= b :texture-unit))
          1
          (and (= b :texture) (= a :texture-unit))
          -1
          :default (compare a b)))
      b)))



(defn eval-op [i x]
  (let [state @(:state i)
        args (-eval i (:args x))
        op (:op x)
        gl (state :gl)
        base-object (first args)]

    (eval-bindings gl state (:bindings x))

    (if-let [f (state op)]
      (apply f args)
      (if-let [m (aget base-object (name op))]
        (.apply m base-object (clj->js (vec (rest args))))
        (throw (js/Error (str "No method found for: " op)))))))


(defrecord Interpreter [state]
  IEval
  (-eval [this x]
    (try
      (cond
        (and (map? x) (:op x))
        (eval-op this x)

        (sequential? x)
        (into [] (map #(-eval this %) x))

        :default
        (if-let [y (@state x)] y x))

      (catch js/Error e
        (throw (js/Error (str "Error evaluating " (pr-str x) ": " (pr-str e))))))))


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



