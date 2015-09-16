(ns gamma.webgl.api
  (:require [gamma.webgl.platform.constants :as c]
            [gamma.webgl.model.core :as m]))

(def id (atom 0))

(defn nid [] (swap! id + 1) @id)

(defn arraybuffer []
  {:tag :arraybuffer :id (nid)})

(defn element-arraybuffer []
  {:tag :element-arraybuffer :id (nid)})

(defn texture
  ([] {:tag :texture :id (nid)})
  ([x] (merge (texture) x)))

(defn texture-image [x]
  (assoc (texture x)
    :target :texture-2d
    :texture-type :image))

(defn texture-pixels [x]
  (assoc (texture x)
    :target :texture-2d
    :texture-type :pixels))

(comment
  (make-fb w h :depth-stencil)
  (framebuffer {:attachments {:color-attachment0 (texture-pixels)}})

  )

(defn framebuffer []
  {:tag :framebuffer :id (nid)})

(defn renderbuffer []
  {:tag :renderbuffer :id (nid)})

(defn input []
  {:tag :input :id (nid)})

(comment
  ;; renderbuffer spec

  {:internalformat x :width x :height y}


  )

(comment
  {:texture-type :image|pixels
   :sampler
                 {:texture-wrap-s :repeat
                  :texture-wrap-t :repeat
                  :texture-min-filter :linear
                  :texture-mag-filter :linear}
   :target x
   :format x
   :type x
   :height x
   :width x})


(defn buffer-data [buffer data]
  [{:arraybuffers {buffer data}}])

(defn texture-data [tex data]
  [{:texture-units {0 {tex {:data data}}}}])

(comment
  [:texture-units 0]
  [:texture-units 0 tex :data]
  [:programs p :attributes]
  [:programs p :uniforms])





(defprotocol IOp
  (exec! [this args]))

(defrecord BufferData [buffer-model]
  IOp
  (exec! [this args]
    (m/conform buffer-model args)))

(defn buffer-data [model buffer]
  (->BufferData (m/resolve-in model [:arraybuffers buffer])))

(defrecord DrawArrays [gl setup-fn uniforms-model]
  IOp
  (exec! [this args]
    (setup-fn)
    (m/conform uniforms-model (:uniforms args))
    (.drawArrays
      gl
      (c/constants
        (:mode args))
      (:start args)
      (:count args))))

(defn draw-arrays-setup [model bindings]
  (let [{:keys [program attributes]} bindings
        program-binding (m/resolve-in model [:bindings])
        attributes-model (m/resolve-in model [:programs program :attributes])]
    (fn []
      (m/conform program-binding {:program program})
      (m/conform attributes-model attributes))))


(defn draw-arrays [model bindings]
  (->DrawArrays
    (:gl model)
    (draw-arrays-setup model bindings)
    (m/resolve-in model [:programs (:program bindings) :uniforms])))




(comment
  (defn draw-arrays [b params]
   (let [{:keys [program framebuffer attributes uniforms]}]
     [{:bindings {:program program}}

      {:texture-units {0 tex}}
      {:programs {p {:attributes attributes
                     :uniforms   uniforms}}}])

   ))

(comment

  (bufferData buff data)
  ;; pass comparison fn here rather than in constructor?
  ;; could also pass data getter and breadcrumbfn in
  (textureData tex data)

  (drawArrays
    {:program p
     :framebuffer f
     :attributes a
     :uniforms x}
    {:start 0 :count 10})



  )











