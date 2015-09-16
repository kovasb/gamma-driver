(ns gamma.webgl.drivers.basic
  (:require
    [gamma.webgl.interpreter :as itr]
    [gamma.webgl.compiler.core :as compiler]
    [gamma.webgl.api :as gd]))



(bufferData [ab1 ab2 ab3])

(run! driver
  (bufferData [ab1 ab2 ab3])
  {ab1 data ab2 data ab3 data})

(drawArrays {:program p
             :framebuffer nil
             :attributes {attr1 ab1 attr2 ab2}})

(run! driver
  (drawArrays {:program p
               :framebuffer nil
               :attributes {attr1 ab1 attr2 ab2}})
  {:uniforms {u1 data1 u2 data2}
   :start 0
   :count 6})

;; should take args or not?
(clear)

;; where do we implement setting the instanced divisor?
;; presumably can override drawArrays or its implementing subfunctions

;; construction process

(fn [subconstructor val]
  (let [program p]
    (subconstructor {:tag :location :shader s :variable v})
    program))

;; if we let runtime representation be clj, can just stuff locations on it

(let [attr1 (attribute "aAttr" :vec2)
      shader (shader attr1)
      ab1 (arraybuffer)
      driver (driver)]
  (run! driver (bufferData [ab1]) {ab1 data})
  (run! driver (drawArrays {:program shader :attributes {attr1 ab1}}) {:first 0 :count 3}))


(defn constructors []
  {:arraybuffer (fn [builder model _] (.createBuffer (b :gl)))
   :program     (fn [b val]
                  (let [s (createShader (b :gl) val)]


                    s
                    ))
   }


  )

(comment
  {:objects                   {id object id2 object}
  :values                    {id value}
  :shaders                   {id
                              {:attribute-bindings {:position x}
                               :uniform-values     x
                               :texture-bindings   x
                               :variable-locations {:position 1}
                               }}
  :bound-shader              id
  :bound-element-arraybuffer id
  :buffer-state              x
  :operations
                             {op {:cached true}}
  :texture-units             {0 tex}

  })

;; want to refer to attribs by keyword rather by val

(variables {:attribute {:position :vec2}
            :uniform {:foo :mat22}
            :varying {:bar :vec2}})
;;
{:position v}
(:position vars)


{:tag :drawArrays
 :program p
 :framebuffer f
 :attributes
      {:position {:buffer buff :layout {:size x :type x :normalized? x :stride x :offset x}}}
 :textures {uniform {:texture t :texture-unit 0}}
 }

{:tag :texture-unit :number 0 :texture t}


(defn variable-locations [gl p])

(defn createProgram [model p]
  (let [gl (@model :gl)
        p1 (install-shader gl p)]
    (swap! model assoc :objects (:id p) p1)
    (swap!
      model
      assoc-in
      [:programs (:id p) :variable-locations]
      (variable-locations gl p))))

(defn useProgram [model p]
  (let [m @model
        gl (:gl m)]
    (if (not= p (:current-program m))
     (if-let [po (get-in m [:objects (:id p)])]
       (do
         (.useProgram gl po)
         (swap! model assoc :current-program p))
       (let [p (createProgram model p)]
         (do
           (.useProgram gl po)
           (swap! model assoc :current-program p)))))))


(defn createFramebuffer [model fb])

(defn currentFramebuffer [model fb]
  (let [m @model
        gl (:gl m)]
    (if (not= (m :bound-framebuffer))
      (let [fbo (if (nil? fb)
                  nil
                  (or
                    (get-in m [:objects (:id fb)])
                    (createFramebuffer model fb)))]
        (.bindFramebuffer gl fbo)
        (swap! model assoc :bound-framebuffer fb)))))

(defn bind-attribute [location buffer layout])

(defn bindAttributes [model p a]
  (let [m @model]
    (when (not= a (get-in m [:programs (:id p) :attributes]))
      (let [l (get-in m [:programs (:id p) :variable-locations])]
        (kv-reduce
          (fn [_ k v]
            (bind-attribute
              (l k)
              ;; assume buffer already exists
              (get-in m [:objects (:id (:buffer v))])
              (:layout v)))
          a)
        (swap! model assoc-in [:programs (:id p) :attributes] a)))))


(defn create-texture [model spec]
  (let [m @model
        gl (m :gl)
        to (.createTexture gl)]
    (.activeTexture gl (:texture-unit spec))
    (.bindTexture gl (constant (:target (:texture spec))) to)
    (swap! model assoc-in [:texture-units texture-unit] texture)




    )

  )


(defn run-textureData [model op input]
  (let [gl (@model :gl)]



    )

  )



(defn bind-texture-uniform [model location {:keys [texture texture-unit]}]
  (let [to (or
             (get-in @model [:objects (:id texture)])
             (createTexture model tex))]
    (if (not= texture (get-in @model [:texture-units texture-unit]))
      (do
        (.activeTexture gl texture-unit)
        (.bindTexture gl texture)
        (swap! model assoc-in [:texture-units texture-unit] texture)))
    (.uniform1i location texture-unit)))


(defn bindTextures [model p u]
  (let [m @model]
    ;; assume no one will bind another texture to same texture unit
    (when (not= u (get-in m [:programs (:id p) :textures]))
      (let [l (get-in m [:programs (:id p) :variable-locations])]
        (kv-reduce
          (fn [_ k v]
            (bind-texture-uniform model (l k) v))
          u)
        (swap! model assoc-in [:programs (:id p) :textures] u)))


    )

  )

(defn bindUniforms [model p uniform-inputs])

(defn run-drawArrays [model op input]
  (useProgram model (:program op))
  (currentFramebuffer model (:framebuffer op))
  (bindAttributes model (:program op) (:attributes op))
  (bindTextures model (:textures op))
  (bindUniforms model p (:uniforms input))
  (drawArrays model x)
  )



(defprotocol IModel
  (resolve [model root v])
  (assert [model root k v]))

(assign [this property ])


(defrecord BindingsModel []
  (assert []
    ;; check if already set
    ;; if need to set -
    ;; -- resolve object & bind gl state
    ;; then assign new value

    )
  )

(defrecord ProgramsModel [l path]
  IModel
  (assign [this r prop val]
    (swap! r assoc-in path l val))
  (resolve [this r v]
    ;; if p is there, return
    ;; if not there, create new p, assign & return
    ;; nb we are returning type not just a map
    ;; consumer will want to resolve p's constituents

    )
  (assert [this r v]
    ;; for each kv
    ;; resolve k

    ))


(defrecord ProgramModel []
  (resolve [this r v]

    )
  (assert [this r v]
    ;; attributes
    ;; uniforms
    ;; texture bindings

    ))


(defrecord AttributesModel [p vlocations])



(resolve model root x)
(assert model root key val)
(assert model root :current-program val)
-> (resolve root (resolve root (resolve root :programs) program) :object)

(assert Programs root )