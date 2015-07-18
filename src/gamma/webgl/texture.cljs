(ns gamma.webgl.texture
  (:require [goog.webgl :as ggl]
            [gamma.webgl.api :as api]))

;; parts of creating texture
;; internal texture specification
;; input data specification: color format, type, image

(comment
  (defrecord TextureUnit [context id]
    api/IOperator
    (operate! [this uniform]
      (.activeTexture (api/gl context) (+ ggl/TEXTURE0 id))
      (.uniform1i (api/gl context) (api/location uniform) id)))

  (defn texture-unit [context id]
    (TextureUnit. context id))

  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

  (defn- texture-unpack [gl spec]
    (let [{:keys [flip-y]} spec]
      (if (not (nil? flip-y))
        (.pixelStorei gl ggl/UNPACK_FLIP_Y_WEBGL flip-y))))


  (def texture-wrap-constants
    {:repeat ggl/REPEAT
     :clamp-to-edge ggl/CLAMP_TO_EDGE
     :mirrored-repeat ggl/MIRRORED_REPEAT})


  (defn- texture-wrap [gl spec]
    (let [{:keys [target s t]} spec]
      (if s
        (.texParameteri
          gl
          target
          ggl/TEXTURE_WRAP_S
          (texture-wrap-constants s)))
      (if t
        (.texParameteri
          gl
          target
          ggl/TEXTURE_WRAP_T
          (texture-wrap-constants t)))))


  (def texture-filter-constants
    {:linear                 ggl/LINEAR
     :nearest                ggl/NEAREST
     :nearest-mipmap-nearest ggl/NEAREST_MIPMAP_NEAREST
     :linear-mipmap-nearest  ggl/LINEAR_MIPMAP_NEAREST
     :nearest-mipmap-linear  ggl/NEAREST_MIPMAP_LINEAR
     :linear-mipmap-linear   ggl/LINEAR_MIPMAP_LINEAR})

  (defn- texture-filter [gl spec]
    (let [{:keys [target min mag]} spec]
      (if min
        (.texParameteri
          gl
          target
          ggl/TEXTURE_MIN_FILTER
          (texture-filter-constants min)))
      (if mag
        (.texParameteri
          gl
          target
          ggl/TEXTURE_MAG_FILTER
          (texture-filter-constants mag)))))

  (defn texture-data-type [d]
    ;; ImageData | HTMLImageElement | HTMLCanvasElement | HTMLVideoElement
    (if (or (instance? js/ImageData d)
            (instance? js/HTMLImageElement d)
            (instance? js/HTMLCanvasElement d)
            (instance? js/HTMLVideoElement d))
      :image
      (if (or (instance? js/Float32Array d) (nil? d))
        :pixels
        (throw (js/Error. (str "texture data type not supported: " (pr-str d)) ))))
    ;; arraybufferview
    )


  (def texture-formats
    {:alpha ggl/ALPHA
     :luminance ggl/LUMINANCE
     :luminance-alpha ggl/LUMINANCE_ALPHA
     :rgb ggl/RGB
     :rgba ggl/RGBA})

  (def texture-data-types
    {:unsigned-byte ggl/UNSIGNED_BYTE
     ; :float ggl/FLOAT
     :unsigned-short-5-6-5 ggl/UNSIGNED_SHORT_5_6_5
     :unsigned-short-4-4-4-4 ggl/UNSIGNED_SHORT_4_4_4_4
     :unsigned-short-5-5-5-1 ggl/UNSIGNED_SHORT_5_5_5_1})


  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


  (defrecord TextureImage2D [context texture spec]
    api/IOperator
    (operate! [this texture-unit]
      (let [gl (api/gl context)
            target ggl/TEXTURE_2D
            {:keys [format-type width height unpack filter wrap faces]} spec
            ;spec (assoc spec :target target)
            [format type] format-type
            format (texture-formats (or format :rgba))
            type (texture-data-types (or type :unsigned-byte))]

        (texture-unpack gl unpack)
        (.activeTexture gl (+ ggl/TEXTURE0 (:id texture-unit)))
        (.bindTexture gl target (api/texture texture))

        (texture-wrap gl (assoc wrap :target target))
        (texture-filter gl (assoc filter :target target))
        (.texImage2D
          gl
          target
          0
          format
          format
          type
          (:data spec)))))


  (defrecord TextureObject [texture]
    api/ITexture
    (texture [this] texture))

  (defn texture-object [ctx]
    (TextureObject. (.createTexture (api/gl ctx))))


  (defrecord FramebufferTexture2D [ctx texture]
    api/IOperator
    (operate! [this texture-unit]
      (.activeTexture (api/gl ctx) (+ ggl/TEXTURE0 (:id texture-unit)))
      (.bindTexture (api/gl ctx) ggl/TEXTURE_2D texture))
    api/IFramebufferAttachment
    (attach [this attachment-point]
      (.framebufferTexture2D
        (api/gl ctx)
        ggl/FRAMEBUFFER
        attachment-point
        ggl/TEXTURE_2D
        texture
        0)))


  (defn framebuffer-texture2d [ctx opts]
    (let [gl (api/gl ctx)
          tex (.createTexture gl)
          target ggl/TEXTURE_2D
          format ggl/RGBA
          type ggl/UNSIGNED_BYTE]
      (.bindTexture gl ggl/TEXTURE_2D tex)
      (.texParameteri gl ggl/TEXTURE_2D ggl/TEXTURE_MIN_FILTER ggl/LINEAR)
      (.texImage2D
        gl
        target
        0
        format
        (:width opts)
        (:height opts)
        0
        format
        type
        nil)
      (FramebufferTexture2D. ctx tex)))

  )




(comment



  (comment
    ;; specific combinations of texture format and texture datatype are allowed
    [
     [:alpha :unsigned-byte]
     [:luminance :unsigned-byte]
     [:luminance-alpha :unsigned-byte]
     [:rgb :unsigned-short-5-6-5]
     [:rgba :unsigned-short-4-4-4-4]
     [:rgba :unsigned-short-5-5-5-1]]
    )


  ;; need: mipmap,
  ;; level-of-detail,
  ;; different cubemap targets
  ;; pixel-store parameters

  (comment
    {:target :2d}
    {:target {:cube-map {:x true}}}
    ;; want to set up cubemap in one step
    {:tag :cube-map :faces {:x [] :y [] :z []}}

    {:tag :texture-2d :data {:faces {:x []}}}
    {:tag :texture-cube-map}

    ;; can texture options vary on a per-face basis?



    )


  (defn texture [gl spec]
    ;(println spec)
    (if (:texture spec)
      spec
      (let [tex (.createTexture gl)
            cube-map? (:faces spec)
            target (if cube-map?
                     ggl/TEXTURE_CUBE_MAP
                     ggl/TEXTURE_2D)
            {:keys [format-type width height unpack filter wrap faces]} spec
            spec (assoc spec :target target)
            [format type] format-type
            format (texture-formats (or format :rgba))
            type (texture-data-types (or type :unsigned-byte))]

        (texture-unpack gl unpack)
        (.activeTexture gl (+ ggl/TEXTURE0 (:texture-id spec)))
        (.bindTexture gl target tex)

        (texture-wrap gl (assoc wrap :target target))
        (texture-filter gl (assoc filter :target target))
        (if cube-map?
          (let [texture-enums [ggl/TEXTURE_CUBE_MAP_POSITIVE_X ggl/TEXTURE_CUBE_MAP_NEGATIVE_X
                               ggl/TEXTURE_CUBE_MAP_POSITIVE_Y ggl/TEXTURE_CUBE_MAP_NEGATIVE_Y
                               ggl/TEXTURE_CUBE_MAP_POSITIVE_Z ggl/TEXTURE_CUBE_MAP_NEGATIVE_Z]
                ;; Make sure we get the faces out in the right order
                texture-faces (reduce into [] [(:x faces) (:y faces) (:z faces)])]
            (dorun (map (fn [face-enum data]
                          (.texImage2D gl face-enum 0 format format type data)) texture-enums texture-faces))
            ;; TODO: Add a check to see if we're supposed to generate mipmaps
            (try
              (.generateMipmap gl target)
              (catch js/Error e
                (js/console.log "Error generating mipmap for texture cube: " e))))
          (case (texture-data-type (:data spec))
            :image
            (.texImage2D
              gl
              target
              0
              format
              format
              type
              (:data spec))
            :pixels
            (.texImage2D
              gl
              target
              0
              format
              width
              height
              0
              format
              type
              (:data spec))))
        (.bindTexture gl target nil)
        (assoc spec :tag :texture :texture tex))))

  )



