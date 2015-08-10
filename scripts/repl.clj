(require '[cljs.repl :as repl])
(require '[cljs.repl.browser])


(repl/repl*
  (cljs.repl.browser/repl-env*
    {:static-dir ["resources/public" "resources/public/js/out"]})
  {:output-dir "resources/public/js/out"
   :main 'gamma.webgl.test
   :asset-path "/js/out"})

  (def hello-triangle
    (p/program
      {:vertex-shader vertex-shader
       :fragment-shader fragment-shader
       :precision {:float :mediump}}
      ))

(println (:glsl (:fragment-shader hello-triangle)))

(defn main []
                                      (let [gl  (.getContext (gdom/getElement "gl-canvas") "webgl")
                                            vs  (.createShader gl ggl/VERTEX_SHADER)
                                            fs  (.createShader gl ggl/FRAGMENT_SHADER)
                                            pgm (.createProgram gl)
                                            xs  (js/Float32Array. #js [-1.0 -1.0 -1.0 1.0 1.0 -1.0 1.0 -1.0 -1.0 1.0 1.0 1.0])
                                            buf (.createBuffer gl)]
                                        (doto gl
                                          (.shaderSource vs (-> hello-triangle :vertex-shader :glsl))
                                          (.compileShader vs)
                                          (.shaderSource fs (-> hello-triangle :fragment-shader :glsl))
                                          (.compileShader fs)
                                          (.attachShader pgm vs)
                                          (.attachShader pgm fs)
                                          (.linkProgram pgm)
                                          (.bindBuffer ggl/ARRAY_BUFFER buf)
                                          (.bufferData ggl/ARRAY_BUFFER xs ggl/STATIC_DRAW)
                                          (.enableVertexAttribArray (.getAttribLocation gl pgm (:name vertex-position)))
                                          (.vertexAttribPointer (.getAttribLocation gl pgm (:name vertex-position))
                                                                2 ggl/FLOAT false 0 0)
                                          (.useProgram pgm)
                                          (.uniform1f (.getUniformLocation gl pgm (:name width)) 640.0)
                                          (.uniform1f (.getUniformLocation gl pgm (:name height)) 480.0)
                                          (.drawArrays ggl/TRIANGLES 0 6))))
