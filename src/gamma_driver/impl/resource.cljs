(ns gamma-driver.impl.resource
  (:require [goog.webgl :as ggl]))


(defn shader [gl spec]
  (let [s (.createShader
                 gl
                 ({:vertex-shader ggl/VERTEX_SHADER
                   :fragment-shader ggl/FRAGMENT_SHADER}
                   (:tag spec)))]
    (if s
      (do
        (.shaderSource gl s (:glsl spec))
        (.compileShader gl s)
        (let [compiled (.getShaderParameter gl s ggl/COMPILE_STATUS)]
          (if compiled
            (assoc spec (:tag spec) s)
            (throw (js/Error. (str "failed to compile " (name (:tag spec)) ":"
                                   (.getShaderInfoLog gl s)))))))
      (throw (js/Error. (str "Unable to create " (name (:tag spec))))))))


(defn program [gl spec]
  (if (:program spec)
    (do
      (.useProgram gl (:program spec))
      spec)
    (let [v (shader gl (assoc (:vertex-shader spec) :tag :vertex-shader))
         f (shader gl (assoc (:fragment-shader spec) :tag :fragment-shader))
         p (.createProgram gl)]
     (.attachShader gl p (:vertex-shader v))
     (.attachShader gl p (:fragment-shader f))
     (.linkProgram gl p)
     (if (.getProgramParameter gl p ggl/LINK_STATUS)
       (do
         (.useProgram gl p)
         (assoc spec :program p
                    :vertex-shader v
                    :fragment-shader f))
       (throw
         (js/Error.
           (str "failed to link program: "
                (.getProgramInfoLog gl p))))))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;




(defn array-buffer [gl spec]
  (let [buffer (or (:array-buffer spec) (.createBuffer gl))]
    (.bindBuffer gl ggl/ARRAY_BUFFER buffer)
    (.bufferData
     gl
     ggl/ARRAY_BUFFER
     (:data spec)
     (or ({:static-draw ggl/STATIC_DRAW :dynamic-draw ggl/DYNAMIC_DRAW} (:usage spec))
         ggl/STATIC_DRAW))
    (assoc spec :array-buffer buffer)))




(defn element-array-buffer [gl spec]
  (let [buffer (or (:element-array-buffer spec) (.createBuffer gl))]
    (.bindBuffer gl ggl/ELEMENT_ARRAY_BUFFER buffer)
    (.bufferData
      gl
      ggl/ELEMENT_ARRAY_BUFFER
      (:data spec)
      (or ({:static-draw ggl/STATIC_DRAW :dynamic-draw ggl/DYNAMIC_DRAW} (:usage spec))
          ggl/STATIC_DRAW))
    (assoc spec :element-array-buffer buffer)))


;; parts of creating texture
;; internal texture specification
;; input data specification: color format, type, image

(defn- texture-unpack [gl spec]
  (let [{:keys [flip-y]} spec]
    (if (not (nil? flip-y))
      (.pixelStorei gl ggl/UNPACK_FLIP_Y_WEBGL flip-y))))


(def texture-wrap-constants
  {:repeat ggl/REPEAT
   :clamp-to-edge ggl/CLAMP_TO_EDGE
   :mirrored-repeat ggl/MIRRORED_REPEAT})


(defn- texture-wrap [gl spec]
  (let [{:keys [s t]} spec]
    (if s
      (.texParameteri
        gl
        ggl/TEXTURE_2D
        ggl/TEXTURE_WRAP_S
        (texture-wrap-constants s)))
    (if t
      (.texParameteri
        gl
        ggl/TEXTURE_2D
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
  (let [{:keys [min mag]} spec]
    (if min
      (.texParameteri
        gl
        ggl/TEXTURE_2D
        ggl/TEXTURE_MIN_FILTER
        (texture-filter-constants min)))
    (if mag
      (.texParameteri
        gl
        ggl/TEXTURE_2D
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
         {:keys [format-type width height unpack filter wrap]} spec
         [format type] format-type
         format (texture-formats (or format :rgba))
         type (texture-data-types (or type :unsigned-byte))]

     (texture-unpack gl unpack)
     (.activeTexture gl (+ ggl/TEXTURE0 (:texture-id spec)))
     (.bindTexture gl ggl/TEXTURE_2D tex)

     (texture-wrap gl wrap)
     (texture-filter gl filter)

     (case (texture-data-type (:data spec))
       :image
       (.texImage2D
         gl
         ggl/TEXTURE_2D
         0
         format
         format
         type
         (:data spec))
       :pixels
       (.texImage2D
         gl
         ggl/TEXTURE_2D
         0
         format
         width
         height
         0
         format
         type
         (:data spec)))
     (.bindTexture gl ggl/TEXTURE_2D nil)
     (assoc spec :tag :texture :texture tex))))



(def renderbuffer-formats
  {:depth-component16 ggl/DEPTH_COMPONENT16
   :rgba4 ggl/RGBA4
   :rgb5-a1 ggl/RGB5_A1
   :rgb565 ggl/RGB565
   :stencil-index8 ggl/STENCIL_INDEX8})

(defn render-buffer [gl spec]
  (let [rb (.createRenderbuffer gl)
        {:keys [width height format]} spec]
    (.bindRenderbuffer gl ggl/RENDERBUFFER rb)
    (.renderbufferStorage
      gl
      ggl/RENDERBUFFER
      (renderbuffer-formats format)
      width
      height)
    (assoc spec :tag :render-buffer :render-buffer rb)))


(defn frame-buffer-attachment [gl fb [attachment attachment-point]]
  (case (:tag attachment)
    :texture
    (.framebufferTexture2D
      gl
      ggl/FRAMEBUFFER
      attachment-point
      ggl/TEXTURE_2D
      (:texture attachment)
      0)

    :render-buffer
    (.framebufferRenderbuffer
      gl
      ggl/FRAMEBUFFER
      attachment-point
      ggl/RENDERBUFFER
      (:render-buffer attachment))))




(defn frame-buffer [gl spec]
  (let [fb (.createFramebuffer gl)
        {:keys [color depth stencil depth-stencil]} spec]
    (.bindFramebuffer gl ggl/FRAMEBUFFER fb)
    (dorun
      (map
       #(frame-buffer-attachment gl fb %)
       (filter
         first
         [[color ggl/COLOR_ATTACHMENT0]
         [depth ggl/DEPTH_ATTACHMENT]
         [stencil ggl/STENCIL_ATTACHMENT]
         [depth-stencil ggl/DEPTH_STENCIL_ATTACHMENT]])))

    ;(println (.checkFramebufferStatus gl ggl/FRAMEBUFFER)
    (.bindFramebuffer gl ggl/FRAMEBUFFER nil)
    (assoc spec :tag :frame-buffer :frame-buffer fb)))




(defn release [gl spec]
  (case (:tag spec)
    :program (.deleteProgram gl (:program spec))
    :array-buffer (.deleteBuffer gl (:array-buffer spec))
    :element-array-buffer (.deleteBuffer gl (:element-array-buffer spec))
    :frame-buffer (.deleteFramebuffer gl (:frame-buffer spec))
    :render-buffer (.deleteRenderbuffer gl (:render-buffer spec))
    :vertex-shader (.deleteShader gl (:vertex-shader spec))
    :fragment-shader (.deleteShader gl (:fragment-shader spec))
    :texture (.deleteTexture gl(:texture spec))))

