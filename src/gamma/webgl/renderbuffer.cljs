(ns gamma.webgl.renderbuffer
  (:require
    [gamma.webgl.api :as api]
    [goog.webgl :as ggl]))


(defrecord RenderBuffer [ctx renderbuffer]
  api/IFramebufferAttachment
  (attach [this attachment-point]
    (let [gl (api/gl ctx)]
      (.framebufferRenderbuffer
       gl
       ggl/FRAMEBUFFER
       attachment-point
       ggl/RENDERBUFFER
       renderbuffer))))


(def renderbuffer-formats
  {:depth-component16 ggl/DEPTH_COMPONENT16
   :rgba4 ggl/RGBA4
   :rgb5-a1 ggl/RGB5_A1
   :rgb565 ggl/RGB565
   :stencil-index8 ggl/STENCIL_INDEX8})

(defn render-buffer [ctx spec]
  (let [gl (api/gl ctx)
        rb (.createRenderbuffer gl)
        {:keys [width height format]} spec]
    (.bindRenderbuffer gl ggl/RENDERBUFFER rb)
    (.renderbufferStorage
      gl
      ggl/RENDERBUFFER
      (renderbuffer-formats format)
      width
      height)
    (RenderBuffer. ctx rb)))




