(ns gamma.webgl.model.textures
  (:require [gamma.webgl.model.core :as m]
            [gamma.webgl.platform.constants :as c]))


(comment
  (textureData {0 tex} data)
  (conform model {:texture-units {0 {tex {:data x}}}})

  (drawArrays {:program 1 :uniforms {sampler {:texture-unit 0 :texture t}}})
  (conform model
           {:uniforms {sampler 0}
            :texture-units {0 t}})

  (drawArrays {:program 1 :framebuffer {:id 1 :color-attachment0 t}})
  (conform model {:current-framebuffer {:id 1 :color-attachment0 t}})
  ;-->
  (resolve-in model [:framebuffers {:id 1 :color-attachment0 t}])

  (def t0 (->TextureUnit root 0))

  (conform t0 {:tag :texture :id 1})
  (conform t0 {{:tag :texture :id 1} {:data x}})
  (conform t0 {:texture t :data x})
  (conform t0 {:texture t})
  )







(defn set-texture-params! [gl target params]
  (reduce-kv
    (fn [_ k v]
      (.texParameteri
        gl
        target
        (c/constants k)
        (c/constants v)))
    nil
    params))








(defrecord TexturePixels [root parts]
  m/IModel
  (conform [this val]
    (let [d (:data val)]
      (let [{:keys [target format type width height]} @parts
            gl (:gl root)]
        (do
          (.texImage2D
            gl
            (c/constants target)
            0
            (c/constants format)
            width
            height
            0
            (c/constants format)
            (c/constants type)
            d)
          (set-texture-params!
            gl
            (c/constants target)
            {:texture-wrap-s :repeat
             :texture-wrap-t :repeat
             :texture-min-filter :linear
             :texture-mag-filter :linear})))))
  (resolve [this val]
    (@parts val)))


(defrecord TextureImage [root parts]
  m/IModel
  (conform [this val]
    (if-let [d (:data val)]
      (let [{:keys [target format type]} @parts
            gl (:gl root)]
        (do
         (.texImage2D
           gl
           (c/constants target)
           0
           (c/constants format)
           (c/constants format)
           (c/constants type)
           d)
         (set-texture-params!
           gl
           (c/constants target)
           {:texture-wrap-s :repeat
            :texture-wrap-t :repeat
            :texture-min-filter :linear
            :texture-mag-filter :linear})))))
  (resolve [this val]
    (@parts val)))


(defn create-texture [root val]
  (let [obj (.createTexture (:gl root))]
    (case (:texture-type val)
      :image (->TextureImage root (atom (assoc val :object obj)))
      :pixels (->TexturePixels root (atom (assoc val :object obj))))))




(defrecord Textures [root parts create-texture]
  m/IModel
  (resolve [this val]
    (if-let [a (@parts val)]
      a
      (let [a (create-texture root val)]
        (swap! parts assoc val a)
        a)))
  (conform [this val]
    (m/delegate m/conform #(m/resolve this %) val)))



(defrecord TextureUnit [root id parts]
  m/IModel
  (conform [this val]
    (let [[tex-spec data]
          (if (= :texture (:tag val))
            [val :none]
            (first val))
          tex (m/resolve-in root [:textures tex-spec])
          gl (:gl root)]

      (.activeTexture gl (+ (c/constants :texture0) id))
      (.bindTexture
        gl
        (c/constants (:target tex-spec))
        (m/resolve tex :object))
      (swap! parts assoc :texture tex-spec)
      (if (not= :none data)
        (m/conform tex data))))
  (resolve [this val]
    (@parts val)))


(defrecord TextureUnits [root parts]
  m/IModel
  (resolve [this val]
    (if-let [a (@parts val)]
      a
      (let [a (->TextureUnit root val (atom {}))]
        (swap! parts assoc val a)
        a)))
  (conform [this val]
    (m/delegate m/conform #(m/resolve this %) val)))




