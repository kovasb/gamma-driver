(ns gamma.webgl.compiler.attribute
  (:require [gamma.webgl.api :as p]
            [goog.webgl :as ggl]
            [gamma.webgl.platform.constants :as c]))



(defn default-layout [attribute]
  {:normalized? false
   :size ({:float 1 :vec2 2 :vec3 3 :vec4 4}
           (:type attribute))
   :type ::c/float
   :offset 0
   :stride 0})



(comment
  (defn bind-attribute [buffer layout location]
   (let [{:keys [size type normalized? stride offset]} layout]
     [[:bindBuffer :gl ::c/array-buffer buffer]
      [:vertexAttribPointer :gl
       location
       size
       type
       normalized?
       stride
       offset]
      [:enableVertexAttribArray :gl location]])))

(defn bind-attribute-instanced [buffer layout location divisor]
  [(bind-attribute buffer layout location)
   [:vertexAttribDivisorANGLE :gl
    {:tag :extension :extension "ANGLE_instanced_arrays"}
    location
    divisor]])


