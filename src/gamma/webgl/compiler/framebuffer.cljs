(ns gamma.webgl.compiler.framebuffer
  (:require [goog.webgl :as ggl]
            [gamma.webgl.api :as api]
            [gamma.webgl.platform.constants :as c]))


(defn bind-fb [fb]
  [:bindFramebuffer :gl ::c/framebuffer fb])

(defn create-fb [fb]
  [[:assign fb [:createFramebuffer :gl]]
   (bind-fb fb)])

(defn attach [fb attachments]
  ;; iterate thru attachments and call appropriate attachment fn
  (map (fn [[k v]]
         (condp = :tag
           :texture2d
           :renderbuffer
           )
         )
       attachments)
  )




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
         (api/attach v (
                         k)))
       nil
       attachments)
     (.bindFramebuffer gl ggl/FRAMEBUFFER nil)
     (Framebuffer. ctx fb attachments))))




