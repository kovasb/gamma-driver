(ns gamma.webgl.routines.symbolic)

(def id (atom 0))

(defn nid [] (swap! id + 1) @id)

(defn arraybuffer []
  {:tag :arraybuffer :id (nid)})

(defn ->path [x]

  {:tag :path :path x})

(defn shader-input [path shader]
  (let [shader-inputs (:inputs shader)
        sid (:id shader)
        attrs
        (filter #(= :attribute (:storage %)) shader-inputs)
        attrs->buffers (into
                         {}
                         (map #(vector (assoc % :shader sid)
                                       (arraybuffer))
                              attrs))
        inputs (merge
                 (into {}
                       (map
                         (fn [x]
                           [(assoc x :shader sid) (assoc x :shader sid)])
                         (:inputs shader)))
                 attrs->buffers)
        ]
    (concat
      ;; :bind-attribute
      (map (fn [[a b]] [:bind-attribute a b]) attrs->buffers)
      (map
        ;; bind uniforms and buffers
        (fn [[k v]]
          (let [k2 (inputs (assoc k :shader sid))]
            [(if (= :variable (:tag k2)) :bind-uniform :bind-arraybuffer)
            k2
            v]))
        (map (fn [x] [x [:get-in :env (->path (conj path x))]])
             shader-inputs)))))




(defn texture-init [texture-unit texture]
  [[texture-unit texture]])

(defn draw-arrays [path fb]
  [[:bind-framebuffer fb]
   [:draw-arrays
    [:get-in :env (->path (conj path :start))]
    [:get-in :env (->path (conj path :count))]]])

(defn default-framebuffer []
  {:tag :default-framebuffer})

(defn draw [path shader]
  (concat
    [[:current-shader shader]]
    (shader-input (conj path (:id shader)) shader)
    (draw-arrays  (conj path :draw) nil)))


