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

(defn bind-attribute [context attrib buffer]
        (let [gl (p/gl context)
              variable (p/variable attrib)
              location (p/location attrib)
              bufferobject (p/arraybuffer buffer)
              {:keys [size type normalized? stride offset]}
              ((or (p/layout buffer) default-layout) variable)]
          (.bindBuffer gl ggl/ARRAY_BUFFER bufferobject)
          (.vertexAttribPointer
            gl
            location
            size
            type
            normalized?
            stride
            offset)
          (.enableVertexAttribArray gl location)))

(comment
  [:bindBuffer :arraybuffer buffer]
  [:vertexAttribPointer
   location
   size
   type
   normalized
   stride
   offset]
  [:enableVertexAttribArray location]

  )



(defn bind-attribute-instanced [context attrib buffer divisor]
  (bind-attribute context attrib buffer)
  (.vertexAttribDivisorANGLE
    ((extensions context)
      "ANGLE_instanced_arrays")
    (api/location attrib)
    divisor))


(defrecord Attribute [context program variable location]
  p/IVariable
  (location [this] location)
  (variable [this] variable))



(defn attribute [context program variable]
  (let [location (.getAttribLocation
                   (p/gl context)
                   (p/program program)
                   (:name variable))]
    (Attribute. context program variable location)))