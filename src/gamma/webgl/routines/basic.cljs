(ns gamma.webgl.routines.basic
  (:require [gamma.webgl.api :as gd]))


(defn shader-init [shader supplied-inputs]
  (let [inputs (into {} (map (fn [x] [x (gd/input)]) (:inputs shader)))]
    {:inputs inputs
     :commands
             (let [assoc-sid (fn [x] (assoc x :shader (:id shader)))]
               (for [[variable the-input] inputs]
                 (cond
                   (= :attribute (:storage variable))
                   (let [ab (gd/arraybuffer)]
                     [(gd/bind-attribute (assoc-sid variable) ab)
                      (gd/bind-arraybuffer ab the-input)])

                   (and (= :sampler2D (:type variable)) (= :uniform (:storage variable)))
                   (gd/bind-texture-uniform (assoc-sid variable) (supplied-inputs variable))

                   (= :uniform (:storage variable))
                   (gd/bind-uniform (assoc-sid variable) the-input))))}))


(defn draw-arrays [fb]
  (let [start (gd/input)
        count (gd/input)]
    {:inputs {:start start :count count}
     :commands
             [(gd/bind-framebuffer fb)
              (gd/draw-arrays start count)]}))


(defn shader-draw [shader]
  (let [shader-init (shader-init shader nil)
        draw-arrays (draw-arrays nil)]
    {:inputs {:shader (:inputs shader-init) :draw (:inputs draw-arrays)}
     :commands
             [(gd/current-shader shader)
              (:commands shader-init)
              (:commands draw-arrays)]}))



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