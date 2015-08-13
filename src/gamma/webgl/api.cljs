(ns gamma.webgl.api)

(def id (atom 0))

(defn nid [] (swap! id + 1) @id)

(defn arraybuffer []
  {:tag ::arraybuffer :id (nid)})

(defn input []
  {:tag ::input :id (nid)})


(defn bind-attribute [attr buffer]
  [::bind-attribute attr buffer])

(defn bind-arraybuffer [buffer data]
  [::bind-arraybuffer buffer data])

(defn bind-uniform [uniform data]
  [::bind-uniform uniform data])

(defn bind-texture-uniform [uniform data]
  [::bind-texture-uniform uniform data])

(defn bind-framebuffer [fb]
  [::bind-framebuffer fb])

(defn draw-arrays [start count]
  [::draw-arrays start count])

(defn current-shader [s]
  [::current-shader s])



