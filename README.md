# HeDoS

This source code deals with solving 3D Travelling Salesman Problem using Genetic Algorithms. It is source code of my MSc thesis from 2006, but I updated it to use Maven and latest libraries. I also fixed some display and genetic algorithm issues.

## Xj3D installation and copying required libraries

- Download _Xj3D installer_ from [Xj3D Download Page](https://sourceforge.net/projects/xj3d/files/installers/)
- Install it and copy the following JARs from _jar_ inside the Xj3D installation folder
  to the _lib_ folder under the project folder:
  - xj3d.browser_2.1.0-nps.jar
  - xj3d.cadfilter_2.1.0-nps.jar
  - xj3d.replica_2.1.0-nps.jar
  - xj3d-2.1-3rdparty-nps.jar
  - xj3d-2.1-nps.jar
- You can run the program now, it gives the following native library error:

  SEVERE: Native code library (32 and 64 bit library) failed to load: java.lang.UnsatisfiedLinkError: no odejava in java.library.path
  ``

You can overcome this issue by using the following VM argument: `-Djava.library.path=${Xj3D_Installation_Path}/natives/Linux/x84_64/`

But this change creates a window closing problem. It is possible to use without this library path.

## My MSc Thesis

It is in Turkish, here is the information for interested readers:

- T. E. Kalayci, [Yapay Zeka Teknikleri Kullanan Üç Boyutlu Grafik Yazılımları için "Extensible 3D" (X3D) ile Bir Altyapı Oluşturulması ve Gerçekleştirimi](https://tekrei.gitlab.io/papers/2006-MSc-thesis.pdf), Ege üniversitesi Bilgisayar Mühendisliği Yüksek Lisans Tezi, İzmir, Türkiye, 2006.
