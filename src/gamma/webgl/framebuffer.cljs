(ns gamma.webgl.framebuffer
  (:require [goog.webgl :as ggl]
            [gamma.webgl.api :as api]))


(defrecord Framebuffer [ctx framebuffer attachments]
  api/IOperator
  (operate! [this _]
    (.bindFramebuffer (api/gl ctx) ggl/FRAMEBUFFER framebuffer)))


(comment
  (frame-buffer {:height x :width y}
                {:color0 (framebuffer-texture2d )
                 })
  )


(defn frame-buffer [ctx opts attachments]
  (let [gl (api/gl ctx)
        fb (.createFramebuffer gl)]
    (.bindFramebuffer gl ggl/FRAMEBUFFER fb)
    (reduce-kv
      (fn [_ k v]
        (api/attach v ({:color0 ggl/COLOR_ATTACHMENT0
                        :depth ggl/DEPTH_ATTACHMENT
                        :stencil ggl/STENCIL_ATTACHMENT
                        :depth-stencil ggl/DEPTH_STENCIL_ATTACHMENT}
                        k)))
      nil
      attachments)
    (.bindFramebuffer gl ggl/FRAMEBUFFER nil)
    (Framebuffer. ctx fb attachments)))




