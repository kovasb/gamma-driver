(ns gamma.webgl.attribute
  (:require [gamma.webgl.api :as p]
            [goog.webgl :as ggl]))

(defn default-layout [attribute]
  {:normalized? false
   :size ({:float 1 :vec2 2 :vec3 3 :vec4 4}
           (:type attribute))
   :type ggl/FLOAT
   :offset 0
   :stride 0})

(defrecord Attribute [context program variable location]
  p/IInput
  (input! [this data]
    (let [{:keys [size type normalized? stride offset]}
          ((or (p/layout data) default-layout) variable)]
      (.bindBuffer (p/gl context) ggl/ARRAY_BUFFER (p/arraybuffer data))
      (.vertexAttribPointer
        (p/gl context)
        location
        size
        type
        normalized?
        stride
        offset)
      (.enableVertexAttribArray (p/gl context) location))))


(defn attribute [context program variable]
  (let [location (.getAttribLocation
                   (p/gl context)
                   (p/program program)
                   (:name variable))]
    (Attribute. context program variable location)))