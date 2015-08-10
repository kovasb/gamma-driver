(ns gamma.webgl.plot
    (:require
    [gamma.api :as g]
    [gamma.program :as p]
    [gamma.webgl.routines.basic :as r]
    [gamma.webgl.shader :as shader]
    [gamma.webgl.drivers.basic :as driver]
    [clojure.walk :as walk]))

(def x (g/varying "x" :float :mediump))
(def y (g/varying "y" :float :mediump))

(def coords (g/attribute "coords" :vec2))
(def position (g/attribute "position" :vec2))

(def subs
  {'+ g/+ '- g/- '* g/*  '/ g/div 'pow g/pow 'cos g/cos 'sqrt g/sqrt
   'sin g/sin 'if g/if '== g/== '> g/> '< g/< 'x x 'y y 't t})

(defn expr->ast [expr]
  (walk/postwalk
    (fn [x]
      (cond
        (sequential? x) (apply (first x) (rest x))
        (subs x) (subs x)
        :default x))
    expr))

(defn color [x]
  (g/if (g/< 0 x)
    (g/vec4 0 x 0 1)
    (g/vec4 0 0 (g/abs x) 1)))

(defn expr-program [expr]
  {:id :plot
   :vertex-shader   {x               (g/swizzle coords :x)
                     y               (g/swizzle coords :y)
                     (g/gl-position) (g/vec4 position 0 1)}
   :fragment-shader {(g/gl-frag-color) (color (expr->ast expr))}
   :precision       {:float :mediump}})

(defn ->float32 [x]
  (js/Float32Array. (clj->js (flatten x))))

(defn get-context [id]
  (.getContext (.getElementById js/document id) "webgl"))

(defn rect [[[left right] [bottom top]]]
  [[left bottom] [right bottom] [left top]
   [right top] [right bottom] [left top]])

(defn plot
  ([expr range]
   (let [prog (assoc (shader/Shader. (p/program (expr-program expr))) :tag :shader)
         ops (r/draw [:root] prog)
         driver (driver/driver
                  {:gl (get-context "gl-canvas")}
                  ops)]
     (driver/exec!
       driver
       {:plot {position (->float32 (rect [[-1 1] [-1 1]]))
               coords   (->float32 (rect range))}
        :draw {:start 0 :count 6}}))))


(comment
  (plot 'x [[-1 1] [-1 1]])


  (plot 'y [[-1 1] [-1 1]])
  (plot '(+ x y) [[-1 1] [-1 1]])

  (println
    (:glsl (:fragment-shader
             (p/program (expr-program '(+ x y))))))

  (plot '(+ (cos x) (cos y)) [[0 10] [-10 10]])

  (plot '(cos (* x y)) [[-5 5] [-5 5]])

  (plot '(sin (* x y)) [[-10 10] [-10 10]])


  (plot '(sin (/ (pow x 2) (pow y 2))) [[-10 10] [0 3]])

  (plot '(* (sin (/ x y)) (pow x 2)) [[-10 10] [-1 1]])


  (plot '(if (< (+ x y) 0) 1 -1) [[-10 10] [-10 10]])

  (plot '(if
           (< (sqrt
                (+ (pow x 2)
                   (pow y 2)))
              10) 1 0)
        [[-10 10] [-10 10]])

  (plot '(if
           (< (+ (pow x 2)
                 (pow y 3))
              2) 1 0)
        [[-2 2] [-2 2]])



  (expr->ast '(+ x y))



  )


(comment

  (defn animation-state []
    (atom {:updater #(assoc-in % [:plot t] )})

    (defn animator [animation-state]
      (let [x @animation-state]
        (if-let [u (:updater x)]
          (do
            (if-let [d (:driver x)]
              (driver/exec! (:driver x) (u (:data x))))
            (js/requestAnimationFrame
              (fn []
                (animator animation-state)))))))))