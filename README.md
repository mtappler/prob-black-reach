# prob-black-reach

## About

This project contains the implementation accompanying our paper "Probabilistic Black-Box Reachability Checking" which was accepted for presentation at RV17 in Seattle, September 2017. Basically, we implemented a black-box testing technique for systems involving both non-deterministic and stochastic behaviour. Therefore, we assume that systems can be modelled as Markov decision processes, i.e. non-determinism arises from the choice of inputs and the systems reacts stochastically to those inputs. For such systems and corresponding reachability properties we infer input-selection strategies to maximise the probability of satisfying a given property. To this end, we combine techniques from model inference, probabilistic model-checking, model-based testing, and statistical model-checking. 

## Structure and Project Status
The project is split into three parts:
* core: contains the implementation of the main functionality. 
* util: contains some utility classes, e.g., for storing relevant data. 
* adapter: contains the implementation of an adapter for simulating MDPs exported via PRISM.

The depencies of the project are listed in Maven-files. 
I am not a Maven-expert, though, so there may be better ways to structure the project.
It is a prototypical implementation, thus it poses some restrictions. 
The implementation of the main functionality is finished as described in our RV paper, but documentation is currently ongoing effort. 
If you are interested in more information, please do not hesitate to contact me. 

