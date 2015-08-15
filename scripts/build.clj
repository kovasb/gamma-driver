(require '[cljs.build.api :as b])
(require '[cljs.repl :as repl])
(require '[cljs.repl.browser :as browser])

(b/build (b/inputs "src" "test")
         {:main 'gamma.webgl.test
          :asset-path "/js"
          :output-to "resources/js/main.js"
          :output-dir "resources/js"
          :verbose true
          :static-fns true})
