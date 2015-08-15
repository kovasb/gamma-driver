(ns gamma.webgl.compiler.uniform
  (:require [gamma.webgl.api :as p]
            [goog.webgl :as ggl]))

(defn uniform-input [type location data]
  (case type
    :bool [:uniform1iv :gl location data]
    :bvec2 [:uniform2iv :gl data location ]
    :bvec3 [:uniform3iv :gl location data ]
    :bvec4 [:uniform4iv :gl location data ]
    :float [:uniform1fv :gl location data ]
    :vec2 [:uniform2fv :gl location data ]
    :vec3 [:uniform3fv :gl location data ]
    :vec4 [:uniform4fv :gl location data ]
    :int [:uniform1iv :gl location data ]
    :ivec2 [:uniform2iv :gl location data ]
    :ivec3 [:uniform3iv :gl location data ]
    :ivec4 [:uniform4iv :gl location data ]
    :mat2 [:uniformMatrix2fv :gl location false data ]
    :mat3 [:uniformMatrix3fv :gl location false data ]
    :mat4 [:uniformMatrix4fv :gl location false data ]
    nil))




