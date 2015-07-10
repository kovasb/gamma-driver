(ns gamma.webgl.renderbuffer
  (:require [goog.webgl :as ggl]))

(comment
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
      (assoc spec :tag :render-buffer :render-buffer rb))))


