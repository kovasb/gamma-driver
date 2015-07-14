(comment

  ;; current set of targets
  {:tag :current-framebuffer}
  {:tag :current-program}
  {:tag :current-element-array-buffer}
  {:tag :current-texture}

  {:tag :program}
  {:tag :arraybuffer}
  {:tag :element-arraybuffer}
  {:tag :framebuffer}
  {:tag :renderbuffer}
  {:tag :texture}
  (:tag :texture-unit :id 0)

  {:tag :attribute}
  {:tag :uniform}
  ;; attached to a given program




  ;; operators
  {:tag :data :type :vec-of-vec :data x}

  {:tag :buffer-subdata}

  {:tag :copy-pixels}
  {:tag :draw-elements}
  {:tag :draw-arrays}


  ;; current set of operations
  [:attribute :arraybuffer]
  [:arraybuffer :data]
  [:element-arraybuffer :data]
  [:uniform :data]
  [:uniform :texture-unit]
  [:texture-unit :texture-operator]
  [:current-program :program]
  [:current-framebuffer :framebuffer]
  [:current-element-arraybuffer :element-arraybuffer]
  [:current-texture :texture-unit]
  [:ctx :draw-arrays]
  [:ctx :draw-elements]


  ;; initialization
  ;; want to store attribute location inside some kind of attribute thing
  ;; what is runtime representation for these things?
  ;; or just have a big ole hashmap?



  ;; instructions
  :uniform1iv
  :getUniformLocation

  (defrecord getUniformLocation [gl program name]
    (eval! [this context]
      (.getUniformLocation (gl context) (program context program) name))
    (code [this context]
      `(.getUniformLocation
         ~(code (gl context))
         ~(code (program context program)) ~(code name))))


[:getUniformLocation :program :name]

[:getUniformLocation x y z]

(getUniformLocation x y z)

{:tag :set :target {} :value y}




  :bindFramebuffer
  :bindBuffer
  :bufferData
  :createBuffer
  :vertexAttribPointer
  :enableVertexAttribArray
  :vertexAttribDivisorANGLE
  :getAttribLocation
  :getExtension
  :drawElements
  :drawArrays
  :createFramebuffer
  :framebufferRenderbuffer
  :createRenderbuffer
  :bindRenderbuffer
  :renderbufferStorage
  :activeTexture
  :uniform1i
  :pixelStorei
  :texParameteri
  :activeTexture
  :bindTexture
  :texImage2D
  :createTexture
  :framebufferTexture2D




  [[target op]
   (draw-arrays X X)
   ;; -> [{:tag :current-framebuffer} {:tag :draw}]
   ]

  (current-framebuffer the-fbo)
  ;; -> [{:tag :current-framebuffer} the-fbo]
  (the-arraybuffer data)

  ;; vs
  (data the-arraybuffer)

  ;; neither is possible if these are plain datastructure

  (texture2D texture-unit spec)

  (framebuffer-attachment fbo :color (fb-texture2D texture-unit spec))
  ;; return values? semantics of composition? who determins evaluation order?







  [{:tag :current-program} {:tag :pop}]

  ;; can write initializations for every resource object
  ;; boil down to instructions, and can remove rebinding/unbinding there
  ;; can add facilities to push/pop current bindings.




  ;; like gamma, each has a constructor fn, and that creates a unique id.
  ;; thus we get identies for each kind of thing that needs one.
  ;; things like current-framebuffer do not get unique ids, bc they are true globals
  ;; likewise texture-unit has an explicit id




  )