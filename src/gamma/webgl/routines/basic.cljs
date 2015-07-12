(ns gamma.webgl.routines.basic
  (:require
    [goog.webgl :as ggl]
    [gamma.webgl.api :as api]
    [gamma.webgl.arraybuffer :as ab]
    [gamma.webgl.operators :as operators]
    [gamma.webgl.shader :as shader]
    [gamma.webgl.draw :as draw]
    [gamma.webgl.texture :as texture]))


(defn dynamic-ops [inputs->targets inputs->operators data]
  (reduce-kv
    (fn [init k v]
      (conj
        init
        [(inputs->targets k) ((inputs->operators k) v)]))
    []
    data))

(defrecord ShaderInput [context static-ops data->targets data->operators]
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



(defn static-ops [variables->resources variables->shader-input]
  (reduce-kv
    (fn [init k v]
      (conj init [(variables->shader-input k) v]))
    []
    variables->resources))


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


(defn shader-input
  ([ctx shader] (shader-input ctx shader {}))
  ([ctx shader opts]
   (let [inputs (api/inputs shader)
         attrs->inputs (select-keys
                         inputs
                         (filter #(= :attribute (:storage %)) (keys inputs)))
         attrs->abs (attrs->abs ctx attrs->inputs)
         ;; creates new local resources

         static-ops (static-ops
                      (merge attrs->abs opts)
                      ;; map inputspec to new targets (buffer or texture unit)
                      inputs
                      ;; original inputs
                      )
         ;; [textureUniform textureUnit]

         data->targets (merge inputs attrs->abs)
         ;; texture-uniform->texture-unit
         data->operators (data->operators data->targets)
         ;; texture-uniform->TextureOperator
         ]
     (ShaderInput. ctx static-ops data->targets data->operators))))



(defrecord BasicDraw [ctx shader routine]
  api/IRoutine
  (ops [this data]
    (concat
      (api/ops routine (:state data))
      [[(shader/current-shader ctx) (operators/->input shader)]
       [ctx (:draw data)]])))

(defn basic-draw [ctx shader]
  (BasicDraw. ctx shader (shader-input ctx shader)))


(defrecord BasicDrawElements [ctx shader shader-input element-ab]
  api/IRoutine
  (ops [this data]
    (concat
      (api/ops shader-input (:shader data))
      [[(shader/current-shader ctx) (operators/->input shader)]
       [(ab/current-element-array ctx) (operators/->input element-ab)]
       [element-ab (operators/->input-vector-vector   (:elements data) :uint16)]
       [ctx (:draw data)]])))

(defn basic-draw-elements [ctx shader]
  (BasicDrawElements. ctx shader
                      (shader-input ctx shader)
                      (ab/element-array-buffer ctx)))


(defrecord TextureInit [ctx texture texture-unit]
  api/IRoutine
  (ops [this data]
    [
     ;[texture-unit texture]
     [texture-unit (texture/TextureImage2D. ctx texture data)]]))

(defn texture-init [ctx texture-unit]
  (TextureInit. ctx (texture/texture-object ctx) texture-unit))

(comment
  {:texture       {}
  :draw-elements {:shader   X
                  :elements X
                  :draw     {}}
  })


(defrecord Routines [routines]
  api/IRoutine
  (ops [this data]
    (mapcat
      (fn [[k r]]
        (api/ops r (data k)))
      routines)))

(defn routines [x]
  (Routines. x))


(defrecord DrawArrays [target]
  api/IRoutine
  (ops [this data]
    [[target (draw/draw-arrays ggl/TRIANGLES (:start data) (:count data))]]))

(defn draw-arrays [ctx]
  (DrawArrays. ctx))


(comment

  (r/routines
    (let [tu0 (texture/texture-unit 0)]
              [[:texture (r/texture-init ctx tu0)]
               [:shader (r/shader-init ctx shader {uniform tu0})]
               [:draw-arrays (r/draw-arrays ctx)]]))

  (RoutineMap. [[:texture ()]])

  )


(comment
  generic spec

  [[:key proc]
   [[op] [op]]
   [:key proc]
   ]


  :static {:texture {}}
  ;; runroll the procs

  [:key target operator-fn]
  ;; then we can selectively unroll

  IResolve
  (resolve proc {})

  ITargets
  (targets proc)


  )
