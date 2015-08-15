(defproject kovasb/gamma-driver "auto"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :deploy-repositories [["clojars" {:sign-releases false}]]
  :dependencies [[org.clojure/clojure "1.7.0-beta2"]
                 [org.clojure/clojurescript "0.0-3292"]
                 [quile/component-cljs "0.2.4"]
                 [kovasb/gamma "0.0-135-10-gfcaf"]
                 ]
  :plugins [[org.clojars.cvillecsteele/lein-git-version "1.0.0"]]
  :source-paths ["src"])
