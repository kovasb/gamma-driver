(ns gamma.webgl.routines.symbolic)

(def id (atom 0))

(defn nid [] (swap! id + 1) @id)

(defn arraybuffer []
  {:tag :arraybuffer :id (nid)})


(defn shader-input [shader input-map]
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
      (map (fn [[k v]] [(inputs (assoc k :shader sid)) v]) input-map))))


(defn texture-init [texture-unit texture]
  [[texture-unit texture]])

(defn draw-arrays [fb data]
  [[{:tag :current-framebuffer} fb]
   [{:tag :current-framebuffer} {:tag :draw-arrays :start (:start data) :count (:count data)}]])

(defn default-framebuffer []
  {:tag :default-framebuffer})

(defn draw [shader input-map]
  (concat
    [[{:tag :current-shader} shader]]
    (shader-input shader (:data input-map))
    (draw-arrays (default-framebuffer) (:draw input-map))))


(comment
  (let [a1 {:tag :variable :storage :attribute :name "foo"}]
    (shader-input
     {:id :s1 :inputs [a1]}
     {a1 :a1-data}))

  (def x
    (let [a1 {:tag :variable :storage :attribute :name "foo" :type :vec2}]
     (draw
       {:id :s1 :inputs [a1]}
       {:data {a1 {:tag :data :data :a1-data}} :draw {:start 0 :count 3}})))

  (require '[gamma.webgl.operations :as ops] :reload)

  (ops/instructions ops/rules x)

  (first ops/rules)
  (first x)

  (ops/match? (first (first ops/rules)) (first x))

  (nth x 1)

  (last (first (filter #(ops/match? (first %) (nth x 1)) ops/rules)))


  )

(comment
  [[current-fb fb]
   [ctx (draw/draw-arrays ggl/TRIANGLES (:start data) (:count data))]])