(ns gamma-webgl-driver.drivers.minimal
  (:require [gamma-webgl-driver.common.resource :as r]
            [gamma-webgl-driver.common.variable :as v]
            [gamma-webgl-driver.common.configure :as c]
            [gamma-webgl-driver.common.draw :as d]
            [gamma-webgl-driver.protocols :as proto]))



(defrecord MinimalDriver [gl]
  proto/WebGLContextDriver
  (configure [this spec] (c/configure gl spec))
  (gl [this] gl)

  proto/WebGLResourceDriver
  (program [this spec] (r/program gl spec))
  (array-buffer [this spec] (r/array-buffer gl spec))
  (element-array-buffer [this spec] (r/element-array-buffer gl spec))
  (texture [this spec] (r/texture gl spec))
  (frame-buffer [this spec] (r/frame-buffer gl spec))
  (render-buffer [this spec] (r/render-buffer gl spec))
  (release [this spec] (r/release gl spec))

  proto/WebGLVariableDriver
  (attribute-input [this program attribute input] (v/attribute-input gl program attribute input))
  (texture-uniform-input [this program uniform input] (v/texture-uniform-input gl program uniform input))
  (uniform-input [this program uniform input] (v/uniform-input gl program uniform input))

  proto/WebGLDrawDriver
  (draw-arrays [this program spec] (d/draw-arrays gl program spec))
  (draw-arrays [this program spec target] (d/draw-arrays gl program spec target))
  (draw-elements [this program spec] (d/draw-elements gl program spec))
  (draw-elements [this program spec target] (d/draw-elements gl program spec target)))







