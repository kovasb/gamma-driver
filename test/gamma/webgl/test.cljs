(ns gamma.webgl.test
  (:require
    [clojure.browser.repl :as repl]))

(enable-console-print!)

(defonce conn
         (repl/connect "http://localhost:9000/repl"))

(comment
  (ns gamma.webgl.test1
    (:require
      [goog.dom :as gdom]
      [goog.webgl :as ggl]
      [gamma.api :as g]
      [gamma.program :as p]
      [gamma.webgl.api :as api]
      [gamma.webgl.draw :as draw]
      [gamma.webgl.context :as context]
      [gamma.webgl.shader :as shader]
      [gamma.webgl.routines.basic :as routine]
      [gamma.webgl.drivers.basic :as driver]))


  (def pos-attribute (g/attribute "posAttr" :vec2))


  (defn example-shader []
    (p/program
      {:vertex-shader {(g/gl-position) (g/vec4 pos-attribute 0 1)}
       :fragment-shader {(g/gl-frag-color) (g/vec4 1 0 0 1)}}))


  (defn example-data []
    {:state {pos-attribute [[-0.5 -0.5] [0.5 -0.5] [0 0]]}
     :draw  (draw/draw-arrays ggl/TRIANGLES 0 3)})

  (defn example-context []
    (context/context
      (.getElementById js/document "gl-canvas")))


  (defn run []
    (let [ctx (example-context)
          shader (shader/shader ctx (example-shader))
          routine (routine/basic-draw ctx shader)
          driver (driver/driver)]
      (api/exec! driver routine (example-data))))

  (run)

  )
