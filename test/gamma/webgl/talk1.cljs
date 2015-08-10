(ns gamma.webgl.talk1
  (:require [gamma.webgl.hello]))



;; represent an input
(def pos (g/attribute "posAttr" :vec2))

;; use the input
(g/vec4 pos 0 1)

;; represent return value
{(g/gl-position) (g/vec4 pos 0 1)}

;; full shader
(defn example-shader []
  {:id              :hello-triangle
   :vertex-shader   {(g/gl-position) (g/vec4 pos 0 1)}
   :fragment-shader {(g/gl-frag-color) (g/vec4 1 0 0 1)}})

;; compile shader
(def the-shader (shader/compile (example-shader)))

;; print glsl
(println (:glsl (:vertex-shader the-shader)))
(println (:glsl (:fragment-shader the-shader )))

;; parameterize shader
(defn example-shader2 [pos]
  {:id              :hello-triangle
   :vertex-shader   {(g/gl-position) (g/vec4 pos 0 1)}
   :fragment-shader {(g/gl-frag-color) (g/vec4 1 0 0 1)}})

(println
  (:glsl
    (:vertex-shader
      (shader/compile
        (example-shader2 pos)))))

(println
  (:glsl
    (:vertex-shader
      (shader/compile
        (example-shader2 (g/attribute "anotherPos" :vec2))))))


;; pass in more complex input
(println (:glsl
           (:vertex-shader
             (shader/compile
               (example-shader2
                 (g/+
                   (g/vec2 1 -1)
                   (g/attribute "another-pos" :vec2)))))))

;; look ma, no side effects!

;; subexpression elimination
(let [x (g/pow (g/swizzle pos :x) 2)]
  (let [s (shader/compile
            {:id              :hello-triangle
             :vertex-shader   {(g/gl-position) (g/vec4 x x 0 1)}
             :fragment-shader {(g/gl-frag-color) (g/vec4 1 0 0 1)}})]
    (println (:glsl (:vertex-shader s)))))


;; statement-expressions
(let [x (g/swizzle pos :x)
      y (g/if (g/> x 0) (g/sin x) (g/cos x))]
  (let [s (shader/compile
            {:id              :hello-triangle
             :vertex-shader   {(g/gl-position) (g/vec4 y y 0 1)}
             :fragment-shader {(g/gl-frag-color) (g/vec4 1 0 0 1)}})]
    (println (:glsl (:vertex-shader s)))))

;; higher order usage

(apply g/vec4 (map g/sin [1 2 3]))