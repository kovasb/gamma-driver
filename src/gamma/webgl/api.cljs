(ns gamma.webgl.api
  (:require [gamma.webgl.platform.constants :as c]))

(def id (atom 0))

(defn nid [] (swap! id + 1) @id)

(defn arraybuffer []
  {:tag :arraybuffer :id (nid)})

(defn texture []
  {:tag :texture :id (nid) :target ::c/texture-2d})

(defn input []
  {:tag :input :id (nid)})

(defn op
  ([o args] (op nil args))
  ([o bindings args]
   (if bindings
     {:op o :args args :bindings bindings}
     {:op   o
     :args args})))

;; Per-Fragment operations
;; Implicit parameters unknown

;(defn blendColor [args])


;; Whole Framebuffer Operations

;(defn clear [fb args])
;(defn clearColor [fb args])

;; Buffer Objects

(defn bufferData [buffer {:keys [data usage]}]
  (let [target ({:arraybuffer         ::c/array-buffer
                 :element-arraybuffer ::c/element-array-buffer}
                 (:tag buffer))]
    {:op       :bufferData
     :bindings {(:tag buffer) buffer}
     :args     [:gl target data usage]}))

;(defn bufferSubData [buffer args])

;; View and Clip
;; Implicit parameters unknown

;(defn viewport [args])

;; Rasterization
;; Implicit parameters unknown

;(defn cullFace [args])

;; Uniforms and attributes

(defn enableVertexAttribArray [{:keys [index]}]
  {:op   :enableVertexAttribArray
   :args [:gl index]})


(defn vertexAttribPointer [buffer {:keys [index size type normalized? stride offset]}]
  {:op       :vertexAttribPointer
   :bindings {:arraybuffer buffer}
   :args     [:gl index size type normalized? stride offset]})


;; Texture Objects

;(defn texImage2D [id texture args])

;; Writing to the Draw Buffer

(defn drawArrays [program framebuffer {:keys [mode first count]}]
  {:op       :drawArrays
   :bindings {:program program :framebuffer framebuffer}
   :args     [:gl mode first count]})


;(defn drawElements [program framebuffer elements args])

;; Special Functions

;(defn enable [args])
;(defn disable [args])
;(defn pixelStorei [args])

;; Renderbuffer Objects

;(defn renderbufferStorage [rb args])

;; Read Back Pixels

;(defn readPixels [fb args])

;; Framebuffer Objects

;(defn framebufferRenderbuffer [fb args])
;(defn framebufferTexture2D [fb args])


;; Util





(comment
  (case type
    :bool [:uniform1iv :gl location data]
    :bvec2 [:uniform2iv :gl data location ]
    :bvec3 [:uniform3iv :gl location data ]
    :bvec4 [:uniform4iv :gl location data ]
    :float [:uniform1fv :gl location data ]
    :vec2 [:uniform2fv :gl location data ]
    :vec3 [:uniform3fv :gl location data ]
    :vec4 [:uniform4fv :gl location data ]
    :int [:uniform1iv :gl location data ]
    :ivec2 [:uniform2iv :gl location data ]
    :ivec3 [:uniform3iv :gl location data ]
    :ivec4 [:uniform4iv :gl location data ]
    :mat2 [:uniformMatrix2fv :gl location false data ]
    :mat3 [:uniformMatrix3fv :gl location false data ]
    :mat4 [:uniformMatrix4fv :gl location false data ]
    nil))

(comment
  (defn bind-attribute [attr buffer]
    [::bind-attribute attr buffer])

  (defn bind-arraybuffer [buffer data]
    [::bind-arraybuffer buffer data])

  (defn bind-uniform [uniform data]
    [::bind-uniform uniform data])

  (defn bind-texture-uniform [uniform data]
    [::bind-texture-uniform uniform data])

  (defn bind-framebuffer [fb]
    [::bind-framebuffer fb])

  (defn draw-arrays [start count]
    [::draw-arrays start count])

  (defn current-shader [s]
    [::current-shader s]))








