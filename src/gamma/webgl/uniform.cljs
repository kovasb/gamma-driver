(ns gamma.webgl.uniform
  (:require [gamma.webgl.api :as p]
            [goog.webgl :as ggl]))

(defn uniform-input [type location data]
  (case type
    :bool [:uniform1iv location data]
    :bvec2 [:uniform2iv data location ]
    :bvec3 [:uniform3iv location data ]
    :bvec4 [:uniform4iv location data ]
    :float [:uniform1fv location data ]
    :vec2 [:uniform2fv location data ]
    :vec3 [:uniform3fv location data ]
    :vec4 [:uniform4fv location data ]
    :int [:uniform1iv location data ]
    :ivec2 [:uniform2iv location data ]
    :ivec3 [:uniform3iv location data ]
    :ivec4 [:uniform4iv location data ]
    :mat2 [:uniformMatrix2fv location false data ]
    :mat3 [:uniformMatrix3fv location false data ]
    :mat4 [:uniformMatrix4fv location false data ]
    nil))

(comment
  (defrecord Uniform [context program variable location]
   p/IVariable
   (location [this] location)
   p/IInput
   (input! [this data]
     ;; inputfn could be statically set at init time
     (uniform-input (p/gl context) (:type variable) location data))))

(comment
  (defn uniform [context program variable]
   (Uniform.
     context
     program
     variable
     (.getUniformLocation
       (p/gl context)
       (p/program program)
       (:name variable)))))



