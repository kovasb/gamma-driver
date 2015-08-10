(ns gamma.webgl.routines.basic
  (:require [gamma.webgl.api :as gd]))



(defprotocol IRoutine
  (query [this])
  (ops [this data]))

(defrecord ShaderInit [shader supplied-inputs]
  IRoutine
  (query [this] (:inputs shader))
  (ops [this data]
    (let [assoc-sid (fn [x] (assoc x :shader (:id shader)))]
      (for [input (:inputs shader)]
        (cond
          (= :attribute (:storage input))
          (let [ab (gd/arraybuffer)]
            [(gd/bind-attribute (assoc-sid input) ab)
             (gd/bind-arraybuffer ab (data input))])

          (and (= :sampler2D (:type input)) (= :uniform (:storage input)))
          (gd/bind-texture-uniform (assoc-sid input) (supplied-inputs input))

          (= :uniform (:storage input))
          (gd/bind-uniform (assoc-sid input) (data input)))))))


(defrecord DrawArrays [fb]
  IRoutine
  (query [this] [:start :count])
  (ops [this data]
    [(gd/bind-framebuffer fb)
     (gd/draw-arrays
       (:start data)
       (:count data))]))





(defrecord ShaderDraw [shader shader-init draw-arrays]
  IRoutine
  (query [this] [{:draw (query draw-arrays)}
                 {:shader (query shader-init)}])
  (ops [this data]
    [(gd/current-shader shader)
     (ops shader-init (:shader data))
     (ops draw-arrays (:draw data))]))


(defn shader-draw [shader]
  (ShaderDraw.
    shader
    (ShaderInit. shader nil)
    (DrawArrays. nil)))


(comment
  (defn draw [path shader]
   (concat
     [(gd/current-shader shader)]
     (shader-input (conj path (:id shader)) shader)
     (draw-arrays (conj path :draw) nil))))

(comment
  {:shader (sf)
   :draw (df)}

  )





;;;;;;;;;;;;;

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