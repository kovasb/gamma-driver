(ns gamma.webgl.routines.basic
  (:require [gamma.webgl.api :as gd]
            [gamma.webgl.platform.constants :as c]))

(defn variable-type [x]
  (cond
    (= :attribute (:storage x)) :attribute
    (and (= :sampler2D (:type x)) (= :uniform (:storage x))) :texture-uniform
    (= :uniform (:storage x)) :uniform))

(defmulti init-variable
          (fn [x params p] (variable-type x)))

(defn default-layout [attribute]
  {:normalized? false
   :size ({:float 1 :vec2 2 :vec3 3 :vec4 4}
           (:type attribute))
   :type ::c/float
   :offset 0
   :stride 0})

(defn bind-attribute [attribute buffer]
  (let [location {:tag :location :variable attribute}]
    [(gd/vertexAttribPointer
       buffer
       (assoc (default-layout attribute) :index location))
     (gd/enableVertexAttribArray {:index location})]))

(defmethod init-variable :attribute [v x _]
  (let [ab (gd/arraybuffer)]
    [(bind-attribute v ab)
     (gd/bufferData ab {:data x :usage ::c/static-draw})]))

(def uniform-scalar-op-map
  {:bool :uniform1iv
   :float :uniform1fv
   :vec2 :uniform2fv
   :vec3 :uniform3fv
   :vec4 :uniform4fv})

(def uniform-matrix-op-map
  {:mat2 :uniformMatrix2fv
   :mat3 :uniformMatrix3fv
   :mat4 :uniformMatrix4fv})

(defn uniform-input [type location data p]
  (if-let [op (uniform-matrix-op-map type)]
    {:op op :bindings {:program p}
     :args [:gl location false data]}
    {:op   (uniform-scalar-op-map type) :bindings {:program p}
    :args [:gl location data]}))

(defmethod init-variable :uniform [v x p]
  (uniform-input (:type v) {:tag :location :variable v} x) p)

(defmethod init-variable :texture-uniform [v x p]
  {:op       :uniform1i
   :bindings (assoc x :program p)
   :args     [:gl {:tag :location :variable v} (:texture-unit x)]})


(comment
  (defmethod init-variable :texture-uniform [x param]
   (gd/bind-texture-uniform (assoc-sid variable) (supplied-inputs variable))))

(comment
  (shader-init shader {sampler {:texture-unit 1 :texture tex}})

  )

;; extend to pull textures in
(defn shader-init [shader constants]
  (let [constants (or constants {})
        inputs (into {}
                     (map
                       (fn [x] [x
                                (constants x (gd/input))])
                       (:inputs shader)))]
    {:inputs (into {} (filter #(not (constants (first %))) inputs))
     :commands
             (for [[variable the-input] inputs]
               (init-variable
                 (assoc variable :shader (:id shader))
                 the-input
                 shader))}))


(defn draw-arrays [shader fb]
  (let [start (gd/input)
        count (gd/input)]
    {:inputs   {:start start :count count}
     :commands [(gd/drawArrays shader fb {:mode ::c/triangles :first start :count count})]}))


(defn shader-draw
  ([shader] (shader-draw nil nil))
  ([shader x] (shader-draw x nil))
  ([shader x fb]
   (let [shader-init (shader-init shader x)
         draw-arrays (draw-arrays shader fb)]
     {:inputs {:shader (:inputs shader-init) :draw (:inputs draw-arrays)}
      :commands
              [(:commands shader-init)
               (:commands draw-arrays)]})))

(comment
  [(gd/current-shader shader)
  {:shader shader-init}
  {:draw draw-arrays}])

(comment
  [:compare :this commands]

  )

(comment
  {:count input :start input}

  )







;;;;;;;;;;;;;;;;;;;;;;;;;

(defn texture-init [texture-unit texture]
  [[texture-unit texture]])

(defn default-framebuffer []
  {:tag :default-framebuffer})



(comment
  (let [tex (texture2d data)]
    [(texture-init [:texture] {0 tex})

     (draw path shader {uniform tex})
     ;; need tex-unit for texture
     ;; something like {:tag :texture-unit :binding texture}
     ;; - to be set up in initialization
     ;; if there is multiple textures at the id?
     ;; need compiler support for that.

     ]))

(comment
  (texture-init texture)
  (shader-init {uniform texture} )
  ;-->
  [:bind-texture-unit (texture-unit texture) texture]

  ;; problem is texture object must be naked somewhere to be inited
  ;; or, we have
  {:tag :texture-unit :texture {:tag :texture :width w}}

  ;; initialization of texture unit bound up with texture
  ;; but that doesn't work for fb textures

  ;; connection with instructions
  [:bind-texture-uniform uniform texture-with-id]
  ;; least problems with this for now..

  (texture-init {}

    )

  (def my-tex (texture-unit 0))

  (textured
    {my-tex :texture2d|spec}
    {:content (shader-init shader {:textures {uni my-tex}})})

  ;desirable to parameterize texture from runtime input, so we don't have to change code?
  ;need to specify kind of texture, so we know what inputs to expect

  {:textures {:foo data :bar data}}

  )

(comment
  (defn ->path [x]
    {:tag :path :path x})


  (defn shader-input
    ([path shader] (shader-input path shader {}))
    ([path shader supplied-inputs]
     (let [assoc-sid (fn [x] (assoc x :shader (:id shader)))
           data-path (fn [x] [:get-in :env (->path (conj path x))])]
       (for [input (:inputs shader)]
         (cond
           (= :attribute (:storage input))
           (let [ab (gd/arraybuffer)]
             [(gd/bind-attribute (assoc-sid input) ab)
              (gd/bind-arraybuffer ab (data-path input))])

           (and (= :sampler2D (:type input)) (= :uniform (:storage input)))
           (gd/bind-texture-uniform (assoc-sid input) (supplied-inputs input))

           (= :uniform (:storage input))
           (gd/bind-uniform (assoc-sid input) (data-path input))))))))