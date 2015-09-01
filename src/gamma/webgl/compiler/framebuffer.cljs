(ns gamma.webgl.compiler.framebuffer
  (:require [goog.webgl :as ggl]
            [gamma.webgl.api :as api]
            [gamma.webgl.platform.constants :as c]))





(comment


  (let [fb (framebuffer)
        tex (texture)
        s1 XX
        s2 XX]
    (init-framebuffer)
    (drawArrays {:framebuffer fb} {})
    (init-shader s2 {sampler tex})
    (drawArrays {} {}))


  (framebufferRenderbuffer
    {:framebuffer fb}
    {:attachment a :renderbuffer r})

  (framebufferTexture2D {:framebuffer fb} {:texture t :tex-target t :level l})



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


(comment
  (defn attach-renderbuffer [binding rb])




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


  )

