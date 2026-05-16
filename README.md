# HeDoS

This source code deals with solving 3D Travelling Salesman Problem using Genetic Algorithms. 

Originally developed for an MSc thesis in 2006, it has been modernized to use **Java 21 (LTS)**, Maven, and structured YAML configuration.

## Key Features

- **Java 21 Modernization**: Utilizes **Virtual Threads** and **Structured Concurrency** for scalable parallel processing.
- **High-Performance Math**: Leverages the **Vector API (SIMD)** for lightning-fast 3D coordinate and fitness calculations.
- **Advanced GA Operators**: Includes **Edge Recombination (ERX)**, **Partially Mapped (PMX)**, and **Cycle (CX)** crossovers specialized for TSP.
- **Hybrid Local Search**: Integrates **Lin-Kernighan**, **2-Opt**, **3-Opt**, and **Partitioned Parallel** local search heuristics.
- **Robust Architecture**: Built with **Google Guice (DI)**, **Jackson (YAML)**, and an **EventBus** for clean separation of concerns.
- **Interactive Visualization**: Real-time performance charting with persistent tooltips and 3D path rendering.

## Getting Started

### Prerequisites

- **Java 21 (LTS)**: Required for Vector API (SIMD) and Virtual Threads.
- **Maven 3.8+**: Used for dependency management and build orchestration.
- **Xj3D Libraries**: See the manual installation section below.

### Build and Run

To compile and launch the application, use the following Maven command:

```bash
mvn clean compile exec:exec
```

## Usage

1. **Targets**: Manage 3D points via the "Manage Targets" dialog or generate random datasets through "File > Generate Random Targets".
2. **Configuration**: Adjust GA parameters (Population, Mutation, Crossover) in the side panel. 
3. **Hybrid GA**: Select a **Local Optimization** strategy (e.g., Lin-Kernighan or Parallel 3-Opt) to significantly improve solution quality.
4. **Analysis**: Use the real-time **Duration Chart** to monitor performance. Click on the chart to view exact generation metrics in a persistent tooltip.
5. **Persistence**: Save your best tour and detailed performance statistics using "File > Save Results".

License: **Apache License 2.0**

---

## Xj3D installation and copying required libraries

- Download _Xj3D installer_ from [Xj3D Download Page](https://savage.nps.edu/Xj3D.nps/jars/README.html)
- Install it and copy the following JARs from _jar_ inside the Xj3D installation folder
  to the _lib_ folder under the project folder:

| File | Suggested ArtifactId |
| :--- | :--- |
| `aviatrix3d-all_3.1.1-nps.jar` | `aviatrix3d-all` |
| `xj3d.browser_2.3.0-nps.jar` | `xj3d-browser` |
| `xj3d.cadfilter_2.3.0-nps.jar` | `xj3d-cadfilter` |
| `xj3d.replica_2.3.0-nps.jar` | `xj3d-replica` |
| `xj3d-2.3-3rdparty-nps.jar` | `xj3d-3rdparty` |
| `xj3d-2.3-nps.jar` | `xj3d-core` |

- You can run the program now, it gives the following native library error:

  SEVERE: Native code library (32 and 64 bit library) failed to load: java.lang.UnsatisfiedLinkError: no odejava in java.library.path
  ``

You can overcome this issue by using the following VM argument: `-Djava.library.path=${Xj3D_Installation_Path}/natives/Linux/x84_64/`

But this change creates a window closing problem. It is possible to use without this library path.

## My MSc Thesis

It is in Turkish, here is the information for interested readers:

- T. E. Kalayci, [Yapay Zeka Teknikleri Kullanan Üç Boyutlu Grafik Yazılımları için "Extensible 3D" (X3D) ile Bir Altyapı Oluşturulması ve Gerçekleştirimi](https://tekrei.gitlab.io/papers/2006-MSc-thesis.pdf), Ege üniversitesi Bilgisayar Mühendisliği Yüksek Lisans Tezi, İzmir, Türkiye, 2006.
