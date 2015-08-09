(ns gamma.webgl.texture
  (:require [goog.webgl :as ggl]
            [gamma.webgl.api :as api]
            [gamma.webgl.constants :as c]))

;; parts of creating texture
;; internal texture specification
;; input data specification: color format, type, image



(defn bind-texture-uniform [location id]
  [[:activeTexture :gl id]
   [:uniformli :gl location id]])

(defn texture-unpack [gl spec]
  (let [{:keys [flip-y]} spec]
    (if (not (nil? flip-y))
      [:pixelStorei
       :gl
       :c/unpack-flip-y-webgl
       flip-y])))

(defn texture-wrap [target spec]
  (let [{:keys [s t]} spec]
    [(if s
       [:texParameteri
        :gl
        target
        :c/texture-wrap-s
        s] [])
     (if t
       [:texParameteri
        :gl
        target
        :c/texture-wrap-t
        s] [])]))

(defn texture-filter [target spec]
  (let [{:keys [min mag]} spec]
    [(if min
       [:texParameteri
        :gl
        target
        :c/texture-min-filter
        min] [])
     (if mag
       [:texParameteri
        :gl
        target
        :c/texture-mag-filter
        mag] [])]))


(defn texture-image-2d [context texture spec  texture-unit]
  (let [gl (api/gl context)
        target :c/texture-2d
        {:keys [format-type width height unpack filter wrap faces]} spec
        ;spec (assoc spec :target target)
        [format type] format-type
        format (or format :c/rgba)
        type (or type :c/unsigned-byte)]

    [(texture-unpack gl unpack)
     [:activeTexture :gl (:id texture-unit)]
     [:bindTexture :gl target texture]
     (texture-wrap target wrap)
     (texture-filter target filter)
     [:texImage2D
      :gl
      target
      0
      format
      format
      type
      (:data spec)]]))


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








