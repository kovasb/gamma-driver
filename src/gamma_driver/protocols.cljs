(ns gamma-driver.protocols)


(defprotocol IContext
  (configure [this spec])
  (gl [this]))

(defprotocol IResource
  (program [this spec])
  (array-buffer [this spec])
  (element-array-buffer [this spec])
  (texture [this spec])
  (frame-buffer [this spec])
  (render-buffer [this spec])
  (release [this resource]))

(defprotocol IBindVariable
  (bind-attribute [this program attribute input])
  (bind-texture-uniform [this program uniform input])
  (bind-uniform [this program uniform input]))

(defprotocol IBind
  (bind [this program spec]))

(defprotocol IDraw
  (draw-arrays [this program spec] [this program spec target])
  (draw-elements [this program spec] [this program spec target]))
