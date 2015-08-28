(ns gamma.webgl.uniform
  (:require
    [gamma.api :as g]
    [gamma.webgl.api :as gd]
    [gamma.webgl.shader :as shader]
    [gamma.webgl.routines.basic :as r]
    [gamma.webgl.drivers.basic :as driver]
    [gamma.webgl.compiler.core]))


(def pos (g/attribute "posAttr" :vec2))
(def color (assoc
             (g/uniform "colorUniform" :vec3)
             :precision  :mediump))

(defn example-shader []
  (shader/compile
    {:id              :uniform
     :vertex-shader   {(g/gl-position) (g/vec4 pos 0 1)}
     :fragment-shader {(g/gl-frag-color) (g/vec4 color 1)}}))

;; Helpers
(defn get-context [id]
  (.getContext
    (.getElementById js/document id)
    "webgl"))

(defn ->float32 [x]
  (js/Float32Array.
    (clj->js (flatten x))))


(defn main []
  (let [r (r/shader-draw (example-shader))
        driver (driver/driver
                 (:commands r)
                 {:gl (get-context "gl-canvas")})]
    (driver/exec!
      driver
      (driver/assoc-inputs
        (:inputs r)
        {:shader {pos (->float32 [0 0 1 0 0 1])
                  color #js [1 1 0]}
         :draw   {:start 0 :count 3}}))
    driver))

(comment

  (main)

  (def r (r/shader-draw (example-shader)))

  (def d (driver/driver
           (:commands r)
           {:gl (get-context "gl-canvas")}))

  (driver/exec!
    d
    (driver/assoc-inputs
      (:inputs r)
      {:shader {pos (->float32 [0 0 1 0 0 1])
                color #js [1 1 0]}
       :draw   {:start 0 :count 3}}))

  (defn state-lookup [driver val]
    (@(:state (:interpreter driver)) val))


  (state-lookup d {:tag :input, :id 8})

  (.uniform3fv
    (state-lookup d :gl)
    (state-lookup d {:tag :location, :variable
                          {:tag :variable, :name "colorUniform", :type :vec3, :storage :uniform, :precision :mediump, :shader :uniform}})
    (state-lookup d {:tag :input, :id 8}))

  (def op {:op   :uniform3fv,
    :args [:gl
           {:tag      :location,
            :variable {:tag       :variable,
                       :name      "colorUniform",
                       :type      :vec3,
                       :storage   :uniform,
                       :precision :mediump,
                       :shader    :uniform}}
           {:tag :input, :id 8}]})

  (gamma.webgl.interpreter/eval-op (:interpreter d) op)

  (if-let [m (aget gl (name op))]
    (.apply m gl (clj->js (vec args)))
    (throw (js/Error (str "No method found for: " op))))

  (aget (state-lookup d :gl) (name :uniform3fv))


  (def gl (get-context "gl-canvas"))

  (:glsl (:vertex-shader (example-shader)))

  (do (def d (main)) 1)
  (r/shader-draw (example-shader))

  (r/shader-init (example-shader) nil)

  (gamma.webgl.compiler.core/compile-loop
    [:gamma.webgl.api/bind-uniform
     {:tag :variable, :name "colorUniform", :type :vec3, :storage :uniform, :precision :mediump, :shader :uniform}
     {:tag :gamma.webgl.api/input, :id 25}])

  (gamma.webgl.compiler.core/compile-loop
    (:commands (r/shader-init (example-shader) nil)))

  )