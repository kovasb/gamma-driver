(ns gamma.webgl.compiler.texture
  (:require [goog.webgl :as ggl]
            [gamma.webgl.api :as gd]
            [gamma.webgl.platform.constants :as c]))

;; parts of creating texture
;; internal texture specification
;; input data specification: color format, type, image



(defn bind-texture-uniform [location id]
  [[:activeTexture :gl id]
   [:uniformli :gl location id]])


(defn flip-y [t val]
  (gd/op
    :pixelStorei
    t
    [:gl ::c/unpack-flip-y-webgl val]))


(defn wrap-s [t val]
  (gd/op
    :texParameteri
    t
    [:gl (:target (:texture t)) ::c/texture-wrap-s val]))

(defn wrap-t [t val]
  (gd/op
    :texParameteri
    t
    [:gl (:target (:texture t)) ::c/texture-wrap-t val]))

(defn min-filter [t val]
  (gd/op
    :texParameteri
    t
    [:gl (:target (:texture t)) ::c/texture-min-filter val]))

(defn mag-filter [t val]
  (gd/op
    :texParameteri
    t
    [:gl (:target (:texture t)) ::c/texture-mag-filter val]))


(defn texImage2D [t format type data]
  (gd/op
    :texImage2D
    t
    [:gl (:target (:texture t)) 0 format format type data]))



(defn textureImage2D-2 [target format width height type data]
  [:texImage2D :gl target 0 format width height 0 format type data])

(defn texture-image [tex spec]
  (let [{:keys [s t min mag format type data]} spec]
    [(flip-y tex 1)
     (wrap-s tex s)
     (wrap-t tex t)
     (min-filter tex min)
     (mag-filter tex mag)
     (texImage2D tex format type data)]))


(comment

  ;; compiler

  (defn get-inputs [input keys]
    (map (into {} (fn [x] [x [:get input x]] keys))))

  (texture-image (get-inputs input [:target :s :t :min :mag :format :type :data]))

  [:bind-texture texture input]

  (texture-image {:target [:get input :target]})

  )


(comment
  (texture-image
    {:target ::c/texture-2d
     :format ::c/rgba
     :type ::c/unsigned-byte
     :s ::c/repeat
     :t ::c/repeat
     :min ::c/linear
     :mag ::c/linear
     :data (api/input)})


  [:bind-texture uniform texture]
  {:tag :texture2d :spec {:s :t}}
  ;; can we designated input to have a certain type?


  )




;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn bind-fb-texture [tu texture]
  [[:activeTexture :gl tu]
   [:bindTexture :gl :c/texture-2d texture]])


(defn attach-fb-texture [texture attachment-point]
  [:framebufferTexture2D
   :gl
   :c/framebuffer
   attachment-point
   :c/texture-2d
   texture
   0])




(defn framebuffer-texture2d [tex ctx opts]
  (let [target :c/texture-2d
        format :c/rgba
        type :c/unsigned-byte]
    [[:bindTexture :gl target tex]
     [:texParameteri :gl target :c/texture-min-filter :c/linear]
     [:texImage2D
      :gl
      target
      0
      format
      (:width opts)
      (:height opts)
      0
      format
      type
      nil]]))








