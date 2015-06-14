(ns gamma-driver.drivers.basic
  (:require
    [gamma-driver.api :as gd]
    [gamma-driver.protocols :as gdp]))



(defn produce [driver constructor-fn resource-spec]
  (let [{:keys [resource-state mapping-fn produce-fn]} driver
        k (mapping-fn resource-spec)
        existing-resource (@resource-state k)
        new-spec (produce-fn driver constructor-fn existing-resource resource-spec)]
    (swap! resource-state assoc k new-spec)
    new-spec))

(defn default-produce-fn [driver constructor-fn old-spec new-spec]
  (if (and (:immutable? old-spec) (:immutable? new-spec))
    old-spec
    (constructor-fn (gd/gl driver) (merge old-spec new-spec))))


(defn input [driver program binder-fn variable new-spec]
  (let [{:keys [input-fn input-state]} driver
        old-spec (get-in @input-state [program variable] {})
        new-spec (input-fn driver program binder-fn variable old-spec new-spec)]
    (swap! input-state assoc-in [program variable] new-spec)))


(defn default-input-fn [driver program binder-fn variable old-spec new-spec]
  (let [t (:tag new-spec)]
    (if (and old-spec new-spec (not= :uniform t) (= (old-spec t) (new-spec t)))
      old-spec
      (if
        (and (= :uniform t) (:immutable? old-spec))
        old-spec
        (binder-fn (gd/gl driver) program variable new-spec)))))



(defn program-inputs-state [driver program]
  (let [s (@(:input-state driver) program)]
    (into {}
          (map #(vector % (s %)) (:inputs program)))))

(defn input-incomplete? [driver program]
  (let [state  (@(:input-state driver) program)
        inputs (:inputs program)]
    (if (some nil? (map state inputs))
      ;; We're going to bail at this point anyway, so let's spend some
      ;; extra time extracting what inputs were incomplete
      (remove (fn [input] (get state input)) inputs)
      false)))
;; would like return value to indicate which inputs are not filled in?


(defn draw-count [driver program]
  ;; for now just pick the first attribute that has a count
  (first
    (keep
      (fn [[k v]]
        (if (= :attribute (:storage k))
          (:count v)))
      (@(:input-state driver) program))))

(defn draw-arrays*
  ([driver program opts]
    (draw-arrays* driver program opts nil))
  ([driver program opts target]
   (if-let [missing-inputs (input-incomplete? driver program)]
     ;; TODO: Warn in dev mode for programs missing :id key
     ;; Should we make this pass the missing inputs so tooling can
     ;; catch it and present it to the user, e.g. in a shadertoy/live
     ;; editor?
     (throw (js/Error. (str "Program '" (pr-str (:id program)) "' inputs are incomplete."  (pr-str (map (juxt :name :type) missing-inputs)) ", given: " (pr-str @(:input-state driver)))))
     (let [c (if-let [c (:count opts)]
               c
               (draw-count driver program))
           new-opts (assoc opts :count c)]
       (gd/draw-arrays
        (gdp/gl driver)
        program
        ;; should supply below as an arg, with defaults
        new-opts
        target)))))


(defn draw-elements*
  ([driver program opts]
    (draw-elements* driver program opts nil))
  ([driver program opts target]
   (if-let [missing-inputs (input-incomplete? driver program)]
     (throw (js/Error. (str "Program '" (pr-str (:id program)) "' inputs are incomplete."  (pr-str (map (juxt :name :type) missing-inputs)))))
     (if-let [index (get-in @(:input-state driver) [program {:tag :element-index}])]
       (do
         (when (not= index (get-in @(:input-state driver) [{:tag :element-index}]))
           (.bindBuffer (:gl driver) ggl/ELEMENT_ARRAY_BUFFER (:element-array-buffer index))
           (swap! (:input-state driver) assoc {:tag :element-index} index))
         (gd/draw-elements
           (gdp/gl driver)
           program
           ;; should supply below as an arg, with defaults
           {:draw-mode  (:draw-mode opts :triangles)
            :first      0
            ;; Should we just throw an error if :index-type isn't specified,
            ;; rather than default to :unsigned-short?  Seems kinder.
            :index-type :unsigned-short
            :count      (:count opts)}
           target))
       (throw (js/Error. (str "Program '" (pr-str (:id program)) "' missing element index")))))))

(defn draw-elements-instanced*
  ([driver program opts]
    (draw-elements-instanced* driver program opts nil))
  ([driver program opts target]
   (if-let [missing-inputs (input-incomplete? driver program)]
     (throw (js/Error. (str "Program '" (pr-str (:id program)) "' inputs are incomplete."  (pr-str (map (juxt :name :type) missing-inputs)))))
     (if-let [index (get-in @(:input-state driver) [program {:tag :element-index}])]
       (do
         (when (not= index (get-in @(:input-state driver) [{:tag :element-index}]))
           (.bindBuffer (:gl driver) ggl/ELEMENT_ARRAY_BUFFER (:element-array-buffer index))
           (swap! (:input-state driver) assoc {:tag :element-index} index))
         (gd/draw-elements-instanced
           (gdp/gl driver)
           program
           ;; should supply below as an arg, with defaults
           {:draw-mode      (:draw-mode opts :triangles)
            :first          0
            ;; Should we just throw an error if :index-type isn't specified,
            ;; rather than default to :unsigned-short?  Seems kinder.
            :index-type     :unsigned-short
            :count          (:count opts)
            :ext            (:ext opts)
            :instance-count (:instance-count opts)}
           target))
       (throw (js/Error. (str "Program '" (pr-str (:id program)) "' missing element index")))))))


(defrecord BasicDriver [gl name resource-state mapping-fn input-state input-fn produce-fn]
  gdp/IContext
  (configure [this spec] (gd/configure gl spec))
  (gl [this] gl)

  gdp/IResource
  (program [this spec]   (gd/program gl spec))
  (array-buffer [this spec] (produce this gd/array-buffer spec))
  (element-array-buffer [this spec] (produce this gd/element-array-buffer spec))
  (texture [this spec] (produce this gd/texture spec))
  (frame-buffer [this spec] (produce this gd/frame-buffer spec))
  (render-buffer [this spec] (produce this gd/render-buffer spec))
  (release [this spec] (let [k (mapping-fn spec)]
                         (gd/release gl spec)
                         (swap! resource-state dissoc k)))

  gdp/IBindVariable
  (bind-attribute [this program attribute spec]
    (input
      this
      program
      gd/bind-attribute
      attribute
      spec))
  (bind-element-array [this program element-array spec]
    (input
      this
      program
      gd/bind-element-array
      element-array
      spec))
  (bind-texture-uniform [this program uniform spec]
    (input
      this
      program
      gd/bind-texture-uniform
      uniform
      spec))
  (bind-uniform [this program uniform spec]
    (input
      this
      program
      gd/bind-uniform
      uniform
      spec))

  gdp/IDraw
  (draw-arrays [this program spec] (draw-arrays* this program spec))
  (draw-arrays [this program spec target] (draw-arrays* this program spec target))
  (draw-elements [this program spec] (draw-elements* this program spec))
  (draw-elements [this program spec target] (draw-elements* this program spec target))
  (draw-elements-instanced [this program spec] (draw-elements-instanced* this program spec))
  (draw-elements-instanced [this program spec target] (draw-elements-instanced* this program spec target)))


(defn basic-driver [gl & [name]]
  (map->BasicDriver
   {:gl             gl
    :name           (or name "default")
    :resource-state (atom {})
    :mapping-fn     (fn [x] (or (:id x) (:element x) x))
    :input-state    (atom {})
    :input-fn       default-input-fn
    :produce-fn     default-produce-fn}))


(comment
  (bind-dispatch-fn {:tag :variable :storage :attribute} nil)
  (bind-dispatch-fn {:tag :variable :storage :uniform} nil)
  (bind-dispatch-fn {:tag :variable :storage :uniform :type :sampler2D} nil)
  (bind-dispatch-fn {:tag :element-index} nil)
  (bind-dispatch-fn {:tag :variable-array} nil))





