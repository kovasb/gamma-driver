(ns gamma.webgl.routines.symbolic)

(def id (atom 0))

(defn nid [] (swap! id + 1) @id)

(defn arraybuffer []
  {:tag :arraybuffer :id (nid)})

(defn ->path [x]
  {:tag :path :path x})

(defn select-attributes [inputs]
  (filter #(= :attribute (:storage %)) inputs))

(defn shader-variables [shader inputs]
  (map #(assoc % :shader (:id shader)) inputs))

(defn attribute-input [attrs attr-buffers]
  (map (fn [a b] [:bind-attribute a b])))

(defn buffer-init [buffers data]
  (map (fn [b d] [:bind-arraybuffer b d]) buffers data))

(defn init-attribute [attribute buffer data]
  [[:bind-attribute attribute buffer]
   [:bind-arraybuffer buffer data]])

(defn init-uniform [uniform data]
  [:bind-uniform uniform data])

(defn init-texture-uniform [uniform texture]
  [:bind-texture-uniform uniform texture])

(defn shader-input
  ([path shader] (shader-input path shader {}))
  ([path shader supplied-inputs]
   (let [assoc-sid (fn [x] (assoc x :shader (:id shader)))
         data-path (fn [x] [:get-in :env (->path (conj path x))])]
     (for [input (:inputs shader)]
       (cond
         (= :attribute (:storage input))
         (init-attribute (assoc-sid input) (arraybuffer) (data-path input))

         (and (= :sampler2D (:type input)) (= :uniform (:storage input)))
         (init-texture-uniform (assoc-sid input) (supplied-inputs input))

         (= :uniform (:storage input))
         (init-uniform (assoc-sid input) (data-path input)))))))

(defn draw-arrays [path fb]
  [[:bind-framebuffer fb]
   [:draw-arrays
    [:get-in :env (->path (conj path :start))]
    [:get-in :env (->path (conj path :count))]]])


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



(defn texture-init [texture-unit texture]
  [[texture-unit texture]])



(defn default-framebuffer []
  {:tag :default-framebuffer})

(defn draw [path shader]
  (concat
    [[:current-shader shader]]
    (shader-input (conj path (:id shader)) shader)
    (draw-arrays  (conj path :draw) nil)))


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