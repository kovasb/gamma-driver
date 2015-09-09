(ns gamma.webgl.model.core)

(defprotocol IModel
  (conform [this val])
  (resolve [this val]))



(defn resolve-in [x p]
  (reduce #(resolve %1 %2) x p))

(defn delegate [f parts v]
  (reduce-kv
    (fn [_ k v]
      (f (parts k) v))
    nil
    v))


