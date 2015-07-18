(ns gamma.webgl.arraybuffer
  (:require [gamma.webgl.api :as api]
            [gamma.webgl.attribute :as attr]
            [goog.webgl :as ggl]))

(defn bind-ab [x]
  [:bindBuffer :gl ggl/ARRAY_BUFFER x])

(defn bind-eab [x]
  [:bindBuffer :gl ggl/ELEMENT_ARRAY_BUFFER x])

(defn arraybuffer-input [ab data]
  [(bind-ab ab)
   [:bufferData :gl ggl/ARRAY_BUFFER data ggl/STATIC_DRAW]])

(defn create-array-buffer [ab]
  [[:assign ab [:createBuffer :gl]]
   (bind-ab ab)])


(defn element-arraybuffer-input [eab data]
  [(bind-eab eab)
   [:bufferData :gl ggl/ELEMENT_ARRAY_BUFFER data ggl/STATIC_DRAW]])

(defn create-element-array-buffer [eab]
  [[:assign eab [:createBuffer :gl]
    (bind-eab eab)]])

