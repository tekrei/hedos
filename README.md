# HeDoS
3D Travelling Salesman Problem solving using Genetic Algorithm - Source code of my master thesis

#### Xj3D installation and copying required libraries
* Download `Xj3D installer` from [Xj3D Download Page](https://sourceforge.net/projects/xj3d/files/installers/) 
* Install it and copy the following JARs from `jar` inside the Xj3D installation folder
 to the `lib` folder under the project folder:
  * xj3d.browser_2.1.0-nps.jar
  * xj3d.cadfilter_2.1.0-nps.jar
  * xj3d.replica_2.1.0-nps.jar
  * xj3d-2.1-3rdparty-nps.jar
  * xj3d-2.1-nps.jar
* You can run the program now, but you may need to define native libraries.
  * If there is the following error: 
  `SEVERE: Native code library (32 and 64 bit library) failed to load: java.lang.UnsatisfiedLinkError: no odejava in java.library.path` 
  use the following VM argument: `-Djava.library.path=${Xj3D_Installation_Path}/natives/Linux/x84_64/`
  But that creates a window closing problem.
