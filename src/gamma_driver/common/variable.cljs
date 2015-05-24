(ns gamma-driver.common.variable
  (:require [goog.webgl :as ggl]))



(defn attribute-location [gl program attribute]
  (.getAttribLocation gl (:program program) (:name attribute)))


(defn default-layout [attribute]
  {:normalized? false
   :size ({:float 1 :vec2 2 :vec3 3 :vec4 4}
           (:type attribute))
   :type ggl/FLOAT
   :offset 0
   :stride 0
   })


(defn attribute-input [gl program attribute input]
  (let [location (attribute-location gl program attribute)
        {:keys [size type normalized? stride offset]}
        ((or (:layout input) default-layout) attribute)]
    (.bindBuffer gl ggl/ARRAY_BUFFER (:array-buffer input))
    (.vertexAttribPointer gl
                          location
                          size
                          type
                          normalized?
                          stride
                          offset)
    (.enableVertexAttribArray gl location)
    input))

(defn element-index-input [gl program indices input]
  (.bindBuffer gl ggl/ELEMENT_ARRAY_BUFFER (:element-array-buffer input))
  input)

(defn uniform-location [gl program uniform]
  (.getUniformLocation gl (:program program) (:name uniform)))


(defn uniform-input [gl program uniform input]
  (let [location (uniform-location gl program uniform)
        type (:type uniform)
        data (:data input)]
    (case type
      :bool (.uniform1iv gl location data)
      :bvec2 (.uniform2iv gl location data)
      :bvec3 (.uniform3iv gl location data)
      :bvec4 (.uniform4iv gl location data)
      :float (.uniform1fv gl location data)
      :vec2 (.uniform2fv gl location data)
      :vec3 (.uniform3fv gl location data)
      :vec4 (.uniform4fv gl location data)
      :int (.uniform1iv gl location data)
      :ivec2 (.uniform2iv gl location data)
      :ivec3 (.uniform3iv gl location data)
      :ivec4 (.uniform4iv gl location data)
      :mat2 (.uniformMatrix2fv gl location false data)
      :mat3 (.uniformMatrix3fv gl location false data)
      :mat4 (.uniformMatrix4fv gl location false data)
      nil)
    input))

;; TODO: Should these be shorter?
;;
;; TODO: Expose these comments in a way that can be picked up by
;; tooling and thrown in errors
(def texture-target
  {:texture-2d                  ggl/TEXTURE_2D ;;Uses a 2D image.
   :texture-cube-map-positive-x ggl/TEXTURE_CUBE_MAP_POSITIVE_X ;; Image for the positive X face of the cube map.
   :texture-cube-map-negative-x ggl/TEXTURE_CUBE_MAP_NEGATIVE_X ;; Image for the negative X face of the cube map.
   :texture-cube-map-positive-y ggl/TEXTURE_CUBE_MAP_POSITIVE_Y ;; Image for the positive Y face of the cube map.
   :texture-cube-map-negative-y ggl/TEXTURE_CUBE_MAP_NEGATIVE_Y ;; Image for the negative Y face of the cube map.
   :texture-cube-map-positive-z ggl/TEXTURE_CUBE_MAP_POSITIVE_Z ;; Image for the positive Z face of the cube map.
   :texture-cube-map-negative-z ggl/TEXTURE_CUBE_MAP_NEGATIVE_Z}) ;; Image for the negative Z face of the cube map.

(def internal-format
  {:alpha           ggl/ALPHA           ;; Each element is a single alpha component. The system converts it to floating point, clamped to the range [0, 1], and assembles it into an RGBA element by placing attaching 0.0 to the red, green and blue channels.
   :luminance       ggl/LUMINANCE       ;; Each element is a single luminance component. The system converts it to floating point value, clamped to the range [0, 1], and assembles it into an RGBA element by placing the luminance value in the red, green and blue channels, and attaching 1.0 to the alpha channel.
   :luminance-alpha ggl/LUMINANCE_ALPHA ;; Each element is an luminance/alpha double. The systems converts each component to floating point, clamped to the range [0, 1], and assembles them into an RGBA element by placing the luminance value in the red, green and blue channels.
   :rgb             ggl/RGB             ;; Red, green, and blue channels.
   :rgba            ggl/RGBA})          ;; Red, green, blue, and alpha (transparency) channels.

(def texture-parameter-names
  {:texture-min-filter         ggl/TEXTURE_MIN_FILTER ;; A texture filter constant to use when a surface is rendered smaller than the corresponding texture bitmap (such as for distant objects). Initial value is gl.NEAREST_MIPMAP_LINEAR.
   :texture-mag-filter         ggl/TEXTURE_MAG_FILTER ;; A texture filter constant to use when a surface is rendered larger than the corresponding texture bitmap (such as for close-up objects). Initial value is gl.LINEAR.
   :texture-wrap-s             ggl/TEXTURE_WRAP_S ;; Sets the wrap parameter for texture coordinate s to either gl.CLAMP or gl.REPEAT. gl.CLAMP causes s coordinates to be clamped to the range [0,1] and is useful for preventing wrapping artifacts when mapping a single image onto an object. gl.REPEAT causes the integer part of the s coordinate to be ignored; WebGL uses only the fractional part, thereby creating a repeating pattern. Border texture elements are accessed only if wrapping is set to gl.CLAMP. Initial value is gl.REPEAT.
   :texture-wrap-t             ggl/TEXTURE_WRAP_T ;; Sets the wrap parameter for texture coordinate t to either gl.CLAMP or gl.REPEAT. Initial value is gl.REPEAT.
   :texture-max-anisotropy-ext ggl/TEXTURE_MAX_ANISOTROPY_EXT});; Use anisotropic filtering. You must call getExtension("gl.TEXTURE_MAX_ANISOTROPY_EXT") first to enable. For more info, see Anisotropic filtering.

(def texture-parameter-values
  {:linear ggl/LINEAR
   :nearest ggl/NEAREST
   :mipmap ggl/LINEAR_MIPMAP_NEAREST})

(def pixel-store-parameter-names
  {:pack-alignment                     ggl/PACK_ALIGNMENT ;; Affects packing of pixel data into memory. The initial param value is 4, but it can be set to 1,2,4, or 8.
   :unpack-alignment                   ggl/UNPACK_ALIGNMENT ;; Affects unpacking of pixel data from memory. The initial param value is 4, but can be set to 1,2,4, or 8.
   :unpack-flip-y-webgl                ggl/UNPACK_FLIP_Y_WEBGL ;; Flips the source data along its vertical axis when texImage2D or texSubImage2D are called when param is true. The initial value for param is false.
   :unpack-premultiply-alpha-webgl     ggl/UNPACK_PREMULTIPLY_ALPHA_WEBGL ;; Multiplies the alpha channel, if it exists, into the other color channels during the data transfer when texImage2D or texSubImage2D are called when param is true. The initial value for param is false.
   :unpack-colorspace-conversion-webgl ggl/UNPACK_COLORSPACE_CONVERSION_WEBGL});; The browser's default colorspace conversion is applied when texImage2D or texSubImage2D are called with an HTMLImageElement texture data source. The initial value for param is BROWSER_DEFAULT_WEBGL. No colorspace conversion is applied when set to NONE.

;; TODO: Should throw an error if insufficient/incorrectly types
;; parameters provide
(defn texture-uniform-input [gl program uniform texture]
  (let [location       (uniform-location gl program uniform)
        id             (:texture-id texture)
        detail-level   (:detail texture 0)
        target         (get texture-target (:target texture :texture-2d))
        format         (get internal-format (:format texture :rgba))
        texture-params (select-keys texture (keys texture-parameter-names))
        pixel-params   (select-keys texture (keys pixel-store-parameter-names))]
    (.activeTexture gl (+ ggl/TEXTURE0 id))
    (.bindTexture gl target (:texture texture))
    ;; Should ggl/UNSIGNED_BYTE be parameterized? Probably...
    (.texImage2D gl target detail-level format format ggl/UNSIGNED_BYTE (:image texture))
    (doseq [[k v] texture-params]
      (.texParameteri gl target (get texture-parameter-names k) (get texture-parameter-values v)))
    (doseq [[k v] pixel-params]
      (.pixelStorei gl (get pixel-store-parameter-names k) v))
    (.generateMipmap gl target)
    (.uniform1i gl location id))
  texture)
