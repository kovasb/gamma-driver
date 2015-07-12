(ns gamma.webgl.arraybuffer
  (:require [gamma.webgl.api :as api]
            [goog.webgl :as ggl]))

(defrecord ArrayBuffer [context arraybuffer]
  api/IInput
  (input! [this data]
    (.bindBuffer (api/gl context) ggl/ARRAY_BUFFER arraybuffer)
    (.bufferData
      (api/gl context)
      ggl/ARRAY_BUFFER
      data
      ggl/STATIC_DRAW))
  api/IArraybuffer
  (arraybuffer [this] arraybuffer)
  (layout [this] nil)
  api/IOperator
  (operate! [this attribute]
    (api/input! attribute this))

  )


(defn array-buffer [context]
  (let [gl (api/gl context)
        ab (.createBuffer gl)]
    (.bindBuffer gl ggl/ARRAY_BUFFER ab)
    (ArrayBuffer. context ab)))


(defrecord ElementArrayBuffer [context arraybuffer]
  api/IInput
  (input! [this data]
    (.bindBuffer (api/gl context) ggl/ELEMENT_ARRAY_BUFFER arraybuffer)
    (.bufferData
      (api/gl context)
      ggl/ELEMENT_ARRAY_BUFFER
      data
      ggl/STATIC_DRAW))
  api/IElementArraybuffer
  (element-arraybuffer [this] arraybuffer))


(defn element-array-buffer [context]
  (let [gl (api/gl context)
        ab (.createBuffer gl)]
    (.bindBuffer gl ggl/ELEMENT_ARRAY_BUFFER ab)
    (ElementArrayBuffer. context ab)))

(defrecord CurrentElementArray [context]
  api/IInput
  (input! [this data]
    (.bindBuffer (api/gl context) ggl/ELEMENT_ARRAY_BUFFER (api/element-arraybuffer data))))


(defn current-element-array [context]
  (CurrentElementArray. context))


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