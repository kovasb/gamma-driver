(ns gamma.webgl.model.framebuffers
  (:require [gamma.webgl.model.core :as m]
            [gamma.webgl.platform.constants :as c]))




(defn attachments [root x]
  (let [gl (:gl root)]
    (reduce-kv
     (fn [_ k v]
       (case (:tag v)
         :texture
         (.framebufferTexture2D
           gl
           (c/constants :framebuffer)
           (c/constants k)
           (c/constants :texture-2d)
           (m/resolve-in root [:textures v :object])
           0)
         :renderbuffer
         (.framebufferRenderbuffer
           gl
           (c/constants :framebuffer)
           (c/constants k)
           (c/constants :renderbuffer)
           (m/resolve-in root [:renderbuffers v :object]))))
     nil
     x)))


(defrecord Framebuffer [root parts]
  m/IModel
  (resolve [this val]
    (@parts val))
  (conform [this val]
    (if-let [x (:attachments val)]
      (do
        (m/conform root {:bindings {:framebuffer val}})
        (attachments root x))
      )))

;;

(defn create-framebuffer [root val]
  (let [o (.createFramebuffer (:gl root))]
    (->Framebuffer root (atom (assoc val :object o)))))

;; circular dependency
;; cannot constructor fb without binding the fb
;; cannot bind fb without having it constructed


(defrecord Framebuffers [root parts]
  m/IModel
  (resolve [this val]
    (if-let [a (@parts val)]
      a
      (let [a (create-framebuffer root val)]
        (swap! parts assoc val a)
        (m/conform a val)
        a)))
  (conform [this val]
    (m/delegate m/conform #(m/resolve this %) val)))



(comment
  (conform bindings {:framebuffer fb})
  (resolve-in root [:framebuffers fb :object])




  )

