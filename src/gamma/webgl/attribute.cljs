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

(defn bind-attribute [buffer layout location]
  (let [{:keys [size type normalized? stride offset]} layout]
    [[:bindBuffer :gl ggl/ARRAY_BUFFER buffer]
    [:vertexAttribPointer :gl
     location
     size
     type
     normalized?
     stride
     offset]
    [:enableVertexAttribArray :gl location]]))

(defn bind-attribute-instanced [buffer layout location divisor]
  [(bind-attribute buffer layout location)
   [:vertexAttribDivisorANGLE :gl
    {:tag :extension :extension "ANGLE_instanced_arrays"}
    location
    divisor]])


