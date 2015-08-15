(require '[cljs.build.api :as b])
(require '[cljs.repl :as repl])
(require '[cljs.repl.browser :as browser])

(cljs.repl/repl
  (browser/repl-env
    :static-dir ["resources/html" "resources"])
  :output-dir "resources/js"
  :asset-path "js"
  :static-fns true)