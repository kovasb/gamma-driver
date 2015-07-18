(ns gamma.webgl.operations
  (:require
    [goog.webgl :as ggl]
    [gamma.webgl.attribute :as attr]
    [gamma.webgl.arraybuffer :as ab]
    [gamma.webgl.framebuffer :as fb]
    [gamma.webgl.draw :as draw]
    [gamma.webgl.shader :as shader]))

(defn map-match? [p x]
  (every? #(= (p %) (x %)) (keys p)))

(defn match? [[tp op] [t o]]
  (and (map-match? tp t) (map-match? op o)))

(def rules
  (partition
    2
    [[{:tag :variable :storage :attribute} {:tag :arraybuffer}]
    (fn [[t o]]
      (attr/bind-attribute
        o
        (attr/default-layout t)
        {:tag :location :variable t}))

     [{:tag :current-shader} {:tag :shader}]
     (fn [[t o]]
       [[:useProgram :gl o]])

    [{:tag :arraybuffer} {:tag :data}]
    (fn [[t o]] (ab/arraybuffer-input t (:data o)))

    [{:tag :current-framebuffer} {:tag :default-framebuffer}]
    (fn [[t o]] (fb/bind-fb nil))

    [nil {:tag :draw-arrays}]
    (fn [[t o]] (draw/draw-arrays ggl/TRIANGLES (:start o) (:count o)))
    ]))

(defn first-match [match-fn rules input]
  (first
    (filter #(match-fn % input) rules)))

(defn instructions [rules operations]
  (map
    (fn [x]
      (let [f (last
                (first-match
                 #(match? (first %1) %2)
                 rules
                 x))]
        (f x)))
    operations))



(def inits
  (partition
    2
    [{:tag :arraybuffer}
    ab/create-array-buffer

    {:tag :shader}
    shader/init-shader




    {}
    (fn [x] [])
    ]))

(comment
  {:tag :variable :storage :attribute}
  (fn [x]
    [[:assign
      {:tag :location :variable x}
      [:getAttribLocation (:shader x) (:name x)]]]))

(defn initialization [rules ops]
  (map
    (fn [x]
      (let [f (last
                (first-match
                 #(map-match? (first %1) %2)
                 rules
                 x))]
        (f x)))
    (set (apply concat ops))))



(comment

  (.getAttribLocation
    (p/gl context)
    (p/program program)
    (:name variable))
  )


(comment
  (map-match? {:a 1} {:a 1})
  (map-match? {:a 1 :b 1} {:a 1})
  (map-match? {:a 1} {:b 1})



  )


(comment

  ([{:tag :variable, :storage :attribute, :name "foo", :shader :s1} {:tag :arraybuffer, :id 6}]
    [{:tag :arraybuffer, :id 6} :a1-data]
    [{:tag :current-framebuffer} {:tag :default-framebuffer}]
    [{:tag :default-framebuffer} {:tag :draw-arrays, :start 0, :count 3}])

  (require '[gamma.webgl.attribute :as attr])
  (require '[gamma.webgl.arraybuffer :as ab])
  (require '[gamma.webgl.framebuffer :as fb])
  (require '[gamma.webgl.draw :as draw])




  (let [t {:tag :variable, :storage :attribute, :name "foo", :shader :s1 :type :vec2}
        o {:tag :arraybuffer :id 5}]
    (attr/bind-attribute o
                         (attr/default-layout t)
                         {:tag :location :variable t}))

  (let [t {:tag :arraybuffer, :id 6}
        o {:tag :data :data :the-data}]
    (ab/arraybuffer-input t o))

  (let [t {:tag :current-framebuffer}
        o {:tag :default-framebuffer}]
    (fb/bind-fb nil))

  (let [t {:tag :default-framebuffer}
        o {:tag :draw-arrays, :start 0, :count 3}]
    (draw/draw-arrays :c/triangles (:start o) (:count o)))



  [[{:tag :variable :storage :attribute} {:tag :arraybuffer}]
  (fn [t o] (attr/bind-attribute t o {:tag :location :variable t} (attr/default-layout o)))
  [{:tag :variable :storage :uniform} {:tag :texture-unit}]
  (fn [t o])
  [{:tag :variable :storage :uniform} {:tag :data}]
  (fn [t o])
  [:arraybuffer _]
  (fn [t o] (arraybuffer-input t o))
  [:current-texture :texture-unit]
  (fn [t o])
  [:current-element-array-buffer :element-array-buffer]
  (fn [t o] [[:bindBuffer ggl/ELEMENT_ARRAY_BUFFER o]])
  [:current-framebuffer :framebuffer]
  (fn [t o])
  [:texture-unit :texture]
  (fn [t o])

  ])
