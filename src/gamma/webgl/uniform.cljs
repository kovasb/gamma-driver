(ns gamma.webgl.uniform
  (:require [gamma.webgl.api :as p]
            [goog.webgl :as ggl]))

(defn uniform-input [gl type location data]
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
    nil))

(defrecord Uniform [context program variable location]
  p/IVariable
  (location [this] location)
  p/IInput
  (input! [this data]
    ;; inputfn could be statically set at init time
    (uniform-input (p/gl context) (:type variable) location data)))

(defn uniform [context program variable]
  (Uniform.
    context
    program
    variable
    (.getUniformLocation
      (p/gl context)
      (p/program program)
      (:name variable))))



