# HeDoS

This source code deals with solving 3D Travelling Salesman Problem using Genetic Algorithms. 

Originally developed for an MSc thesis in 2006, it has been modernized to use **Java 25 (LTS)**, Maven, and structured YAML configuration.

## Key Features

- **Java 25 Modernization**: Utilizes **Virtual Threads**, finalized **Structured Concurrency**, and **Scoped Values** for scalable, safe, and efficient parallel execution.
- **High-Performance Math & Memory**: Leverages the **Vector API (SIMD)** for lightning-fast calculations and the **Foreign Function & Memory (FFM) API** for optimized off-heap data management.
- **Robust Architecture**: Built with **Google Guice (DI)**, **Jackson (YAML)**, and an **EventBus** for clean separation of concerns.
- **Interactive Visualization**: Real-time performance charting with persistent tooltips and 3D path rendering.

### Why the FFM API?

Traditional Java arrays are indexed by `int`, limiting them to approximately 2.1 billion elements. For large-scale 3D TSP problems, the $N^2$ distance matrix can easily exceed this limit. By using the **FFM API** (`MemorySegment`):
- **Beyond the 2GB Limit**: We utilize `long` addressing to support massive coordinate datasets.
- **GC Efficiency**: Memory is allocated via `Arena.ofAuto()`, providing a deterministic lifecycle that reduces Garbage Collection overhead during heavy local search iterations.
- **Native Hardware Alignment**: `MemorySegment` allows for precise memory alignment, which is critical for the **Vector API** to perform "aligned loads," resulting in maximum SIMD throughput.

## Optimization Algorithms

HeDoS implements several state-of-the-art algorithms for solving the 3D Travelling Salesman Problem:

### Crossover Operators
- [Cycle Crossover (CX)](./src/main/java/hedos/ga/crossover/CycleCrossover.java): Ensures that all genes in the offspring come from the same position in one of the parents. [Oliver et al. (1987)](https://dl.acm.org/doi/10.5555/645511.657158)
- [Edge Recombination (ERX)](./src/main/java/hedos/ga/crossover/EdgeRecombination.java): Focuses on preserving edge adjacencies, which is critical for TSP. [Whitley et al. (1989)](https://dl.acm.org/doi/10.5555/645511.657094)
- [Partially Mapped Crossover (PMX)](./src/main/java/hedos/ga/crossover/PartiallyMappedCrossover.java): An approach that respects absolute positions and maintains valid permutations. [Goldberg & Lingle (1985)](https://dl.acm.org/doi/10.5555/645511.657102)
- [Single Point Crossover](./src/main/java/hedos/ga/crossover/SinglePointCrossover.java): Splits parent chromosomes at a single point and exchanges segments to produce offspring, maintaining gene order and diversity.
- [Two Point Crossover](./src/main/java/hedos/ga/crossover/TwoPointCrossover.java): Swaps two segments between parents to produce offspring, preserving relative order and diversity.
- [Uniform Crossover](./src/main/java/hedos/ga/crossover/UniformCrossover.java): Randomly selects genes from each parent with equal probability, promoting genetic diversity in offspring.
- [Vectorized Uniform Crossover](./src/main/java/hedos/ga/crossover/VectorizedUniformCrossover.java): A custom high-performance uniform crossover utilizing **Java Vector API (SIMD)** for rapid offspring generation.

### Mutation Operators
- [Displacement Mutation](./src/main/java/hedos/ga/mutation/DisplacementMutation.java): Extracts a sub-tour and re-inserts it at a random position, effectively shifting segments of the tour. [Michalewicz (1992)](https://doi.org/10.1007/978-3-662-03315-9)
- [Inversion Mutation](./src/main/java/hedos/ga/mutation/InversionMutation.java): Reverses the order of a randomly selected subset of genes, helping to eliminate path crossings and improve solution quality.
- [Only Improving Random Mutation](./src/main/java/hedos/ga/mutation/OnlyImprovingRandomMutation.java): Applies random mutations only if they result in an improved solution, ensuring non-decreasing fitness.
- [Only Improving Systematic Mutation](./src/main/java/hedos/ga/mutation/OnlyImprovingSystematicMutation.java): Systematically explores possible mutations and applies only those that improve the solution, ensuring non-decreasing fitness.
- [Random Mutation](./src/main/java/hedos/ga/mutation/RandomMutation.java): Randomly alters genes in the chromosome to introduce genetic diversity and prevent premature convergence.
- [Vectorized Scramble Mutation](./src/main/java/hedos/ga/mutation/VectorizedScrambleMutation.java): Randomly reorders a subset of genes to maintain genetic diversity. [Syswerda (1991)](https://dl.acm.org/doi/10.5555/110243.110271). Our implementation is **SIMD-accelerated** for bulk gene processing.

### Selection Operators
- [Roulette Wheel Selection](./src/main/java/hedos/ga/selection/RouletteWheelSelection.java): Selects individuals probabilistically based on fitness proportion, giving higher chances to fitter individuals. [Goldberg (1989)](https://dl.acm.org/doi/10.5555/534133)
- [Stochastic Universal Sampling](./src/main/java/hedos/ga/selection/StochasticUniversalSampling.java): Selects multiple individuals in a single pass using evenly spaced pointers, ensuring a more representative sampling of the population. [Baker (1987)](https://doi.org/10.1016/0020-0255(87)90073-3)
- [Tournament Selection](./src/main/java/hedos/ga/selection/TournamentSelection.java): Randomly selects a group of individuals and chooses the best among them, balancing selection pressure and diversity. [Goldberg & Deb (1991)](https://dl.acm.org/doi/10.5555/645511.657096)

### Local Search Optimizers (LSO)
- [2-Opt](./src/main/java/hedos/ga/lso/TwoOptOptimization.java): A basic local search that iteratively removes edge crossings by swapping pairs of edges. [Croes (1958)](https://doi.org/10.1287/opre.6.6.791)
- [3-Opt](./src/main/java/hedos/ga/lso/BestThreeOptOptimization.java): A more powerful heuristic that explores triplet edge removals. Our implementation utilizes `java.util.stream` for **Parallel Best-Improvement** scanning across the tour. [Lin (1965)](https://doi.org/10.1287/opre.13.3.457)
- [Lin-Kernighan (LKH)](./src/main/java/hedos/ga/lso/LinKernighanOptimization.java): Often considered the gold standard for TSP, this is a variable-k-opt heuristic. [Lin & Kernighan (1973)](https://doi.org/10.1287/opre.21.2.498)
- [Partitioned Parallel 2-Opt](./src/main/java/hedos/ga/lso/PartitionedTwoOptOptimization.java): A specialized implementation that divides the tour into segments and optimizes them concurrently using modern Java concurrency primitives.
- [Multi Start Local Search](./src/main/java/hedos/ga/lso/MultiStartLocalSearch.java)

## Getting Started

### Prerequisites

- **Java 25 (LTS)**: Required for the latest performance improvements, Vector API (SIMD), and Virtual Threads.
  - **JVM Flags**: Because the project utilizes incubator and preview features, the following flags are required:
    - `--add-modules jdk.incubator.vector`
    - `--enable-preview`
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

License: [**Apache License 2.0**](./LICENSE)

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
