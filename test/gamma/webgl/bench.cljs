(ns gamma.webgl.bench
  (:require
    [gamma.api :as g]
    [gamma.webgl.api :as gd]
    [gamma.webgl.shader :as shader]
    [gamma.webgl.routines.basic :as r]
    [gamma.webgl.drivers.basic :as driver]
    [gamma.webgl.compiler.core]
    [gamma.webgl.compiler.texture :as tex]
    [gamma.webgl.platform.constants :as c]
    [thi.ng.geom.core :as geom]))




(def pos (g/attribute "posAttr" :vec2))

(def tmatrix (g/uniform "u_TriangleMatrix" :mat4))

(defn example-shader []
  (shader/compile
    {:id              :hello-triangle
     :vertex-shader   {(g/gl-position) (g/* tmatrix (g/vec4 pos 0 1))}
     :fragment-shader {(g/gl-frag-color) (g/vec4 1 0 0 1)}}))

;; Helpers
(defn get-context [id]
  (.getContext
    (.getElementById js/document id)
    "webgl"))

(defn ->float32 [x]
  (js/Float32Array.
    (clj->js (flatten x))))



(defn ->radians [degrees]
  (/ (* degrees Math/PI) 180))

(defn rotate [m deg]
  (geom/rotate-around-axis m [0 1 0] (->radians deg)))

(require '[thi.ng.geom.core.matrix :as mat :refer [M44]])

(rotate M44 1)

(do (def points (let [step 0.01]
                  (for [x (range -1 1 step) y (range -1 1 step)]
                    [[x y] [(+ x step) y] [x (+ y step)]])))
    1)

(def parray (->float32 points))
(def pcount (* 3 (count points)))

(def r (r/shader-draw (example-shader) {} nil))
(def d (driver/driver
         (:commands r)
         {:gl (get-context "gl-canvas")}))

(defn drawfn [m]
  (driver/exec!
   d
   (driver/assoc-inputs
     (:inputs r)
     {:shader {pos     (->float32 points)
               tmatrix m}
      :draw   {:start 0 :count pcount}})))

(drawfn (rotate M44 45))

(def animate? (atom true))

(defn animate [drawfn stepfn current-value]
  (js/requestAnimationFrame
    (fn []
      (let [next-value (stepfn current-value)]
        (drawfn next-value)
        (if @animate? (animate drawfn stepfn next-value))))))

(reset! animate? true)

(animate drawfn #(rotate % 1) M44)

(reset! animate? false)
