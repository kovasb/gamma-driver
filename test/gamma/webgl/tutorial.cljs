(ns gamma.webgl.tutorial
  (:require
    [gamma.api :as g]
    [gamma.webgl.api :as gd]
    [gamma.webgl.shader :as shader]
    [gamma.webgl.routines.basic :as r]
    [gamma.webgl.drivers.basic :as driver]
    [gamma.webgl.compiler.core]))


(def pos (g/attribute "posAttr" :vec2))

(defn example-shader []
  {:id              :hello-triangle
   :vertex-shader   {(g/gl-position) (g/vec4 pos 0 1)}
   :fragment-shader {(g/gl-frag-color) (g/vec4 1 0 0 1)}})

;; Helpers
(defn get-context [id]
  (.getContext
    (.getElementById js/document id)
    "webgl"))

(defn ->float32 [x]
  (js/Float32Array.
    (clj->js (flatten x))))




(comment

  (def ops
    (let [shader (shader/compile (example-shader))
         ab (gd/arraybuffer)
         attribute (assoc pos :shader (:id shader))
         data (->float32 [0 0 0 1 1 0])
         start 0
         count 3]
     [(gd/current-shader shader)
      (gd/bind-attribute attribute ab)
      (gd/bind-arraybuffer ab data)
      (gd/bind-framebuffer nil)
      (gd/draw-arrays start count)]))

  (gamma.webgl.compiler.core/compile ops)

  (def driver
    (driver/driver
      {:gl (get-context "gl-canvas")}
      ops))

  (driver/exec! driver {})

  ;; routines abstract over ops

  (def routine (r/shader-draw (shader/compile (example-shader))))

  (r/query routine)

  (def ops
    (r/ops routine {:draw   {:start 0 :count 3}
                    :shader {pos (->float32 [0 0 0 1 1 0])}}))
  
  (def driver
    (driver/driver
      {:gl (get-context "gl-canvas")}
      ops))

  (driver/exec! driver {})

  ;; plug runtime variables into routines

  ;; want something conceptually similar the below
  ;; where we look up the values in an environment at runtime
  (r/ops routine {:draw   {:start 'start :count 'count}
                  :shader {pos 'pos-data}})


  (defn pathify [path spec]
    (cond
      (vector? spec)
      (into {} (map
                 (fn [v]
                   (if (map? v)
                     (let [k (ffirst v)]
                       [k (pathify (conj path k) (last (first v)))])
                     [v [:get-in :env {:tag :path :path (conj path v)}]]))
                 spec))

      (set? spec)
      (into {} (map
                 (fn [v]
                   [v [:get-in :env {:tag :path :path (conj path v)}]])
                 spec))))

  (pathify [:root] (r/query routine))

  (def ops2 (r/ops routine (pathify [:root] (r/query routine))))

  ops2

  (def driver
    (driver/driver
      {:gl (get-context "gl-canvas")}
      ops2))

  (driver/exec! driver {:draw {:start 0 :count 3}
                        :shader {pos (->float32 [0 0 1 0 0 1])}})








  )