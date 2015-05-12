(ns gamma-driver.common.draw
  (:require [goog.webgl :as ggl]))


(defn clear [gl spec])

(defn mask [gl spec])


(def draw-modes
  {:lines ggl/LINES
   :line-strip ggl/LINE_STRIP
   :line-loop ggl/LINE_LOOP
   :points ggl/POINTS
   :triangle-strip ggl/TRIANGLE_STRIP
   :triangle-fan ggl/TRIANGLE_FAN
   :triangles ggl/TRIANGLES})

(defn draw-arrays
  ([gl program opts]
   (.useProgram gl (:program program))
    (.drawArrays
      gl
      (draw-modes (:draw-mode opts))
      (:first opts)
      (:count opts)))
  ([gl program opts target]
   (.bindFramebuffer gl ggl/FRAMEBUFFER (:frame-buffer target))
   (draw-arrays gl program opts)
   (.bindFramebuffer gl ggl/FRAMEBUFFER nil)
   target))

(def element-types
  {:unsigned-byte ggl/UNSIGNED_BYTE
   :unsigned-short ggl/UNSIGNED_SHORT})

(defn draw-elements
  ([gl program opts]
   (.useProgram gl (:program program))
   (.drawElements
     gl
     (draw-modes (:draw-mode opts))
     (:count opts)
     (element-types (:type opts))
     (* ({:unsigned-byte 1 :unsigned-short 2} (:type opts)) (:first opts))))
  ([gl program opts target]
    (.bindFramebuffer gl ggl/FRAMEBUFFER (:frame-buffer target))
    (draw-elements gl program opts)
    (.bindFramebuffer gl ggl/FRAMEBUFFER nil)
    target))






