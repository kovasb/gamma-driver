(ns gamma-driver.protocols)


(defprotocol WebGLContextDriver
  (configure [this spec])
  (gl [this]))

(defprotocol WebGLResourceDriver
  (program [this spec])
  (array-buffer [this spec])
  (element-array-buffer [this spec])
  (texture [this spec])
  (frame-buffer [this spec])
  (render-buffer [this spec])
  (release [this resource]))

(defprotocol WebGLVariableDriver
  (attribute-input [this program attribute input])
  (element-index-input [this program attribute input])
  (texture-uniform-input [this program uniform input])
  (uniform-input [this program uniform input]))

(defprotocol WebGLDrawDriver
  (draw-arrays [this program spec] [this program spec target])
  (draw-elements [this program spec] [this program spec target]))
