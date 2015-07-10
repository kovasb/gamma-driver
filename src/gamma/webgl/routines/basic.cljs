(ns gamma.webgl.routines.basic
  (:require
    [goog.webgl :as ggl]
    [gamma.webgl.api :as api]
    [gamma.webgl.arraybuffer :as ab]
    [gamma.webgl.operators :as operators]
    [gamma.webgl.shader :as shader]
    [gamma.webgl.draw :as draw]))


(defn dynamic-ops [inputs->targets inputs->operators data]
  (reduce-kv
    (fn [init k v]
      (conj
        init
        [(inputs->targets k) ((inputs->operators k) v)]))
    []
    data))

(defrecord BasicRoutine [context static-ops data->targets data->operators]
  api/IRoutine
  (ops [this data]
    (concat
      static-ops
      (dynamic-ops data->targets data->operators data))))


(defn attrs->abs [ctx attrs->inputs]
  (reduce-kv
    (fn [init k v]
      (assoc init k (ab/array-buffer ctx)))
    {}
    attrs->inputs))

(defn attrs-setup-ops [attrs->abs attrs->inputs]
  (reduce-kv
    (fn [init k v]
      (conj init [v (operators/->input (attrs->abs k))]))
    []
    attrs->inputs))

(defn static-ops [attrs->abs attrs->inputs]
  (attrs-setup-ops attrs->abs attrs->inputs))


(defn data->operators [data->targets]
  (reduce-kv
    (fn [init k v]
      (assoc
        init
        k
        (cond
          (= :attribute (:storage k))
          operators/->input-vector-vector
          (= :uniform (:storage k))
          operators/->input-vector)))
    {}
    data->targets))


(defn basic-routine [ctx shader]
  (let [inputs (api/inputs shader)
        attrs->inputs (select-keys
                        inputs
                        (filter #(= :attribute (:storage %)) (keys inputs)))
        attrs->abs (attrs->abs ctx attrs->inputs)
        static-ops (static-ops attrs->abs attrs->inputs)
        data->targets (merge inputs attrs->abs)
        data->operators (data->operators data->targets)
        ]
    (BasicRoutine. ctx static-ops data->targets data->operators)))

(defrecord BasicDraw [ctx shader routine]
  api/IRoutine
  (ops [this data]
    (concat
      (api/ops routine (:state data))
      [[(shader/current-shader ctx) (operators/->input shader)]
       [ctx (:draw data)]])))

(defn basic-draw [ctx shader]
  (BasicDraw. ctx shader (basic-routine ctx shader)))

