(ns gamma-driver.api
  (:require
    [gamma-driver.protocols :as gdp]
    [gamma-driver.impl.context :as context]
    [gamma-driver.impl.resource :as resource]
    [gamma-driver.impl.variable :as variable]
    [gamma-driver.impl.bind :as bind]
    [gamma-driver.impl.draw :as draw]))


;; IContext

(defn configure [this spec]
  (if (satisfies? gdp/IContext this)
    (gdp/configure this spec)
    (context/configure this spec)))

(defn gl
  "Returns WebGL context object."
  [this]
  (if (satisfies? gdp/IContext this)
    (gdp/gl this)
    gl))

;; IResource

(defn program
  "Sets the gl context's current program to spec, compiling spec if it does not contain a webgl program object at :program. Returns spec with webgl program object at :program."
  [this spec]
  (if (satisfies? gdp/IResource this)
    (gdp/program this spec)
    (resource/program this spec)))

(defn array-buffer
  "Populates and returns an array-buffer model suitable as input to attribute-input. Resuses webgl array buffer object supplied at :array-buffer, otherwise creates new object."
  [this spec]
  (if (satisfies? gdp/IResource this)
    (gdp/array-buffer this spec)
    (resource/array-buffer this spec)))

(defn element-array-buffer
  "Populates and returns an element-array-buffer model. Resuses webgl element array buffer object supplied at :element-array-buffer, otherwise creates new object."
  [this spec]
  (if (satisfies? gdp/IResource this)
    (gdp/element-array-buffer this spec)
    (resource/element-array-buffer this spec)))

(defn texture
  "Populates and returns a texture model suitable as input to texture-uniform-input. "
  [this spec]
  (if (satisfies? gdp/IResource this)
    (gdp/texture this spec)
    (resource/texture this spec)))

(defn frame-buffer
  "Populates and returns a frame-buffer model"
  [this spec]
  (if (satisfies? gdp/IResource this)
    (gdp/frame-buffer this spec)
    (resource/frame-buffer this spec)))

(defn render-buffer
  "Populates and returns a render-buffer model."
  [this spec]
  (if (satisfies? gdp/IResource this)
    (gdp/render-buffer this spec)
    (resource/render-buffer this spec)))

(defn release
  "Releases webgl objects associated with model."
  [this resource]
  (if (satisfies? gdp/IResource this)
    (gdp/release this resource)
    (resource/release this resource)))


;; IBindVariable

(defn bind-attribute
  "Binds attribute variable to data in array-buffer-model."
  [this program attribute array-buffer-model]
  (if (satisfies? gdp/IBindVariable this)
    (gdp/bind-attribute this program attribute array-buffer-model)
    (variable/bind-attribute this program attribute array-buffer-model)))

(defn bind-texture-uniform
  "Binds texture uniform variable (sampler2D) to data in texture-model."
  [this program uniform texture-model]
  (if (satisfies? gdp/IBindVariable this)
    (gdp/bind-texture-uniform this program uniform texture-model)
    (variable/bind-texture-uniform this program uniform texture-model)))

(defn bind-uniform
  "Binds uniform variable to data."
  [this program uniform data]
  (if (satisfies? gdp/IBindVariable this)
    (gdp/bind-uniform this program uniform data)
    (variable/bind-uniform this program uniform data)))

;; IBind

(defn bind
  "Binds the program's variables as specified in spec. "
  [this prog spec]
  (if (satisfies? gdp/IBind this)
    (gdp/bind this program spec)
    (bind/bind
      ;; need to pass these in to avoid cyclic dependency
      {:program program
       :array-buffer array-buffer
       :element-array-buffer element-array-buffer
       :texture texture
       :bind-attribute       bind-attribute
       :bind-texture-uniform bind-texture-uniform
       :bind-uniform         bind-uniform}
      this prog spec)))


;; IDraw

(defn draw-arrays
  ([this program spec]
    (if (satisfies? gdp/IDraw this)
      (gdp/draw-arrays this program spec)
      (draw/draw-arrays this program spec)))
  ([this program spec target]
   (if (satisfies? gdp/IDraw this)
     (gdp/draw-arrays this program spec target)
     (draw/draw-arrays this program spec target))))

(defn draw-elements
  ([this program spec]
   (if (satisfies? gdp/IDraw this)
     (gdp/draw-elements this program spec)
     (draw/draw-elements this program spec)))
  ([this program spec target]
   (if (satisfies? gdp/IDraw this)
     (gdp/draw-elements this program spec)
     (draw/draw-elements this program spec))))







