(ns gamma.webgl.arraybuffer
  (:require [gamma.webgl.api :as p]
            [goog.webgl :as ggl]))


(defrecord ArrayBuffer [context arraybuffer]
  p/IInput
  (input! [this data]
    (.bufferData
      (p/gl context)
      ggl/ARRAY_BUFFER
      data
      ggl/STATIC_DRAW))
  p/IArraybuffer
  (arraybuffer [this] arraybuffer)
  (layout [this] nil))

(defn array-buffer [context]
  (let [gl (p/gl context)
        ab (.createBuffer gl)]
    (.bindBuffer gl ggl/ARRAY_BUFFER ab)
    (ArrayBuffer. context ab)))





(comment
  (defn element-array-buffer [gl spec]
    (let [buffer (or (:element-array-buffer spec) (.createBuffer gl))]
      (.bindBuffer gl ggl/ELEMENT_ARRAY_BUFFER buffer)
      (let [data (:data spec)
            data (if (.-buffer data)
                   data
                   (js/Uint16Array. (clj->js (flatten data))))]
        (.bufferData
          gl
          ggl/ELEMENT_ARRAY_BUFFER
          data
          (or ({:static-draw ggl/STATIC_DRAW :dynamic-draw ggl/DYNAMIC_DRAW} (:usage spec))
              ggl/STATIC_DRAW)))
      (assoc spec :element-array-buffer buffer :tag :element-array-buffer)))
  )