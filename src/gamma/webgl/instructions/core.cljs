(ns gamma.webgl.instructions.core)



(defn bindBuffer [a b c]
  (.bindBuffer a b c))

(defn bufferData [a b c d]
  (.bufferData a b c d))

(defn createBuffer [a]
  (.createBuffer a))

(defn vertexAttribPointer [a b c d e f g]
  (.vertexAttribPointer a b c d e f g))

(defn getAttribLocation [a b c]
  (.getAttribLocation a b c))

(defn enableVertexAttribArray [a b]
  (.enableVertexAttribArray a b))

(defn drawArrays [a b c d]
  (.drawArrays a b c d))

(defn drawElements [a b c d e]
  (.drawElements a b c d e))

(defn bindFramebuffer [a b c]
  (.bindFramebuffer a b c))