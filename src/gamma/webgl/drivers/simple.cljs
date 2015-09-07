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

{:objects {id object id object}
 :values {id value}
 :shaders {id
           {:attribute-bindings {:position x}
            :uniform-values x
            :texture-bindings x
            :variable-locations {:position 1}
            }}
 :bound-shader id
 :bound-element-arraybuffer id
 :buffer-state x
 :operations
 {op {:cached true}}

 }

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
      [:shaders (:id p) :variable-locations]
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

(defn currentFramebuffer [model fb]
  (let [m @model
        gl (:gl m)]
    (if (not= (m :bound-framebuffer))
      (if fb
        (if-let [fbo (get-in m [:objects (:id fb)])]
          (do
            (.bindFramebuffer gl nil)
            
            )

          )
        (.bindFramebuffer gl nil)
        )

      )

    )

  )


(defn run-drawArrays [model op input]
  (useProgram model (:program op))
  (currentFramebuffer model (:framebuffer op))
  (bindAttributes model (:attributes op))
  (bindTextures model (:textures op))
  (bindUniforms model p (:uniforms input))
  )


