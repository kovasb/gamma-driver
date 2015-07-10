# gamma-driver

[![Join the chat at https://gitter.im/kovasb/gamma](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/kovasb/gamma?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

The WebGL API is a toxic brew of 9-argument functions and hidden mutable state. Gamma-WebGL presents a simple model of the fundamental GL machine that can be easily manipulated via Clojurescript.  

Gamma-WebGL addresses the following problems of WebGL:
- api ergonomics 
- debugging 
- optimization
- abstraction

#The Model 

Gamma-WebGL embraces the fundamentally procedural, banging-on-hidden-state model of computational exposed by WebGL, and reifies these operations with Clojurescript data and protocols. 

Our model is composed of three concepts: 1) state & operators, 2) routines, and 3) drivers.

## 1. State & Operators

The GPU is a black hole that data dissapears into. WebGL objects such as arraybuffers and textures are just pointers to GPU memory; each kind of object has a particular set of interfaces for sending data in. 

Thus we have various kinds of state, and operators that modify that state. Operators don't get to read the state, they just set it.

In Gamma-WebGL we model state and operators as Clojure records. Operators implement IOperator:

```(operate! operator state)```

for instance, to draw into the default renderbuffer:

```(operate! (DrawArrays. context start count) (DefaultFramebuffer. context))```

Pairs of ```[state operator]``` are called operations, and are a simple, declarative specification that is easy to fabricate, reason about, and optimize over. 

## 2. Routines 

Routines are the mechanism for generating operations in bulk. They automate common patterns of WebGL programming by providing an abstract interface to generating sequences of operations. 

Routines are simply records that implement IRoutine:

```(ops routine data) -> [ [state1 operator1] [state2 operator2] ...]```

Routines take data, construct operators with it, and match it to states. The structure of the input data is wholly determined by the particular routine. This interface allows an arbitrary, abstract specification of the desired state that is decoupled from its implementation.  

A simple function would suffice to fabricate a sequence of operations. However, to insulate the caller from the details of state, the function would have to close over state and other subroutines, and thus could not be introspected or reasoned about.

## 3. Drivers

Drivers execute routines. They iterate through the operations returned by routines and call operate!.

Drivers implement IDriver:

```(exec! driver routine data)```

Drivers provide a simple, powerful hook for modifying the behavior of the system. They can maintain their own state, and thus determine which operations are redundant. They can also wrap or replace both state and operators before invoking them.

