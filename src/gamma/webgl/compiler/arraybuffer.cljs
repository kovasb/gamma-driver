(ns gamma.webgl.compiler.arraybuffer
  (:require [gamma.webgl.api :as api]
            [gamma.webgl.compiler.attribute :as attr]
            [goog.webgl :as ggl]
            [gamma.webgl.platform.constants :as c]))

(defn bind-ab [x]
  [:bindBuffer :gl ::c/array-buffer x])

(defn bind-eab [x]
  [:bindBuffer :gl ::c/element-array-buffer x])

(defn arraybuffer-input [ab data]
  [(bind-ab ab)
   [:bufferData :gl ::c/array-buffer data ::c/static-draw]])

(defn create-array-buffer [ab]
  [[:assign ab [:createBuffer :gl]]
   (bind-ab ab)])


(defn element-arraybuffer-input [eab data]
  [(bind-eab eab)
   [:bufferData :gl ::c/element-array-buffer data ::c/static-draw]])

(defn create-element-array-buffer [eab]
  [[:assign eab [:createBuffer :gl]
    (bind-eab eab)]])

