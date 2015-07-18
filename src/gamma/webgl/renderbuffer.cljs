(ns gamma.webgl.renderbuffer
  (:require
    [gamma.webgl.api :as api]
    [goog.webgl :as ggl]))

(defn attach-rb [rb attachment-point]
  [:framebufferRenderbuffer
   ggl/FRAMEBUFFER
   attachment-point
   ggl/RENDERBUFFER
   rb])

(defn create-rb [rb]
  [[:assign rb [:createRenderbuffer]]
   [:bindRenderbuffer ggl/RENDERBUFFER rb]
   [:renderbufferStorage
    ggl/RENDERBUFFER
    (:format rb)
    (:width rb)
    (:height rb)]])



(def renderbuffer-formats
  {:depth-component16 ggl/DEPTH_COMPONENT16
   :rgba4 ggl/RGBA4
   :rgb5-a1 ggl/RGB5_A1
   :rgb565 ggl/RGB565
   :stencil-index8 ggl/STENCIL_INDEX8})



