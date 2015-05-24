(ns gamma-driver.impl.variable
  (:require [goog.webgl :as ggl]))



(defn attribute-location [gl program attribute]
  (.getAttribLocation gl (:program program) (:name attribute)))


(defn default-layout [attribute]
  {:normalized? false
   :size ({:float 1 :vec2 2 :vec3 3 :vec4 4}
           (:type attribute))
   :type ggl/FLOAT
   :offset 0
   :stride 0
   })


(defn bind-attribute [gl program attribute input]
  (let [location (attribute-location gl program attribute)
        {:keys [size type normalized? stride offset]}
        ((or (:layout input) default-layout) attribute)]
    (.bindBuffer gl ggl/ARRAY_BUFFER (:array-buffer input))
    (.vertexAttribPointer gl
                          location
                          size
                          type
                          normalized?
                          stride
                          offset)
    (.enableVertexAttribArray gl location)
    input))

(defn element-index-input [gl program indices input]
  (.bindBuffer gl ggl/ELEMENT_ARRAY_BUFFER (:element-array-buffer input))
  input)

(defn uniform-location [gl program uniform]
  (.getUniformLocation gl (:program program) (:name uniform)))


(defn bind-uniform [gl program uniform input]
  (let [location (uniform-location gl program uniform)
        type (:type uniform)
        data (:data input)]
    (case type
      :bool (.uniform1iv gl location data)
      :bvec2 (.uniform2iv gl location data)
      :bvec3 (.uniform3iv gl location data)
      :bvec4 (.uniform4iv gl location data)
      :float (.uniform1fv gl location data)
      :vec2 (.uniform2fv gl location data)
      :vec3 (.uniform3fv gl location data)
      :vec4 (.uniform4fv gl location data)
      :int (.uniform1iv gl location data)
      :ivec2 (.uniform2iv gl location data)
      :ivec3 (.uniform3iv gl location data)
      :ivec4 (.uniform4iv gl location data)
      :mat2 (.uniformMatrix2fv gl location false data)
      :mat3 (.uniformMatrix3fv gl location false data)
      :mat4 (.uniformMatrix4fv gl location false data)
      nil)
    input))



(defn bind-texture-uniform [gl program uniform texture]
  (let [location (uniform-location gl program uniform)
        id (:texture-id texture)]
    (.activeTexture gl (+ ggl/TEXTURE0 id))
    (.bindTexture gl ggl/TEXTURE_2D (:texture texture))
    (.uniform1i gl location id))
  texture)
