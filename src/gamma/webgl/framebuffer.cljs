(ns gamma.webgl.framebuffer
  (:require [goog.webgl :as ggl]))

(comment
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

  )



