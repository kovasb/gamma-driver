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
      attrs->buffers
      (map
        (fn [[k v]] [(inputs (assoc k :shader sid)) v])
        (map (fn [x] [x [:get-in :env (->path (conj path x))]])
             shader-inputs)))))




(defn texture-init [texture-unit texture]
  [[texture-unit texture]])

(defn draw-arrays [path fb]
  [[{:tag :current-framebuffer} fb]
   [{:tag :current-framebuffer} {:tag   :draw-arrays
                                 :start [:get-in :env (->path (conj path :start))]
                                 :count [:get-in :env (->path (conj path :count))]}]])

(defn default-framebuffer []
  {:tag :default-framebuffer})

(defn draw [path shader]
  (concat
    [[{:tag :current-shader} shader]]
    (shader-input (conj path (:id shader)) shader)
    (draw-arrays  (conj path :draw) (default-framebuffer))))


