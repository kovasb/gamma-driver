(ns gamma.webgl.compiler.renderbuffer
  (:require
    [gamma.webgl.api :as api]
    [goog.webgl :as ggl]
    [gamma.webgl.platform.constants :as c]))

(defn attach-rb [rb attachment-point]
  [:framebufferRenderbuffer
   ::c/framebuffer
   attachment-point
   ::c/renderbuffer
   rb])

(defn create-rb [rb]
  [[:assign rb [:createRenderbuffer]]
   [:bindRenderbuffer ::c/renderbuffer rb]
   [:renderbufferStorage
    ::c/renderbuffer
    (:format rb)
    (:width rb)
    (:height rb)]])







