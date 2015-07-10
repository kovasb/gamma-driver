(ns gamma.webgl.api)


(defprotocol IRoutine
  (ops [this data]))
;; not necessary for now

(defprotocol IOperator
  (operate! [this target]))

(defprotocol IInput
  (input! [this data]))

(defprotocol IDriver
  (exec! [this target operator]))
;; not necessary for now

;;;;;;;;;;;;;;;;;

(defprotocol IContext
  (gl [this])
  (extensions [this]))

(defprotocol IProgram
  (program [this])
  (inputs [this]))

(defprotocol IArraybuffer
  (arraybuffer [this])
  (layout [this]))

(defprotocol ITexture
  (texture [this]))

(defprotocol IVariable
  (location [this]))



