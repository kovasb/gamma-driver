(ns gamma.webgl.framebuffer
  (:require [goog.webgl :as ggl]
            [gamma.webgl.api :as api]))


(defn bind-fb [fb]
  [:bindFramebuffer :gl ggl/FRAMEBUFFER fb])

(defn create-fb [fb]
  [[:assign fb [:createFramebuffer :gl]]
   (bind-fb fb)])

(defn attach [fb])

(comment
  (frame-buffer {:height x :width y}
                {:color0 (framebuffer-texture2d )
                 })
  )


(comment
  (defn frame-buffer [ctx opts attachments]
   (let [gl (api/gl ctx)
         fb (.createFramebuffer gl)]
     (.bindFramebuffer gl ggl/FRAMEBUFFER fb)
     (reduce-kv
       (fn [_ k v]
         (api/attach v ({:color0        ggl/COLOR_ATTACHMENT0
                         :depth         ggl/DEPTH_ATTACHMENT
                         :stencil       ggl/STENCIL_ATTACHMENT
                         :depth-stencil ggl/DEPTH_STENCIL_ATTACHMENT}
                         k)))
       nil
       attachments)
     (.bindFramebuffer gl ggl/FRAMEBUFFER nil)
     (Framebuffer. ctx fb attachments))))




