Reah Rajmangal
Operating Systems Lab 4 README

The source code consists of three java files and requires Java 1.8.* in order to run.
NOTE: This is important for this lab, since default methods are used in an interface.
Pager.java is the main file that needs to run.

***
In the working directory (RR-LAB-4), use the following commands:
-> module load java-1.8 (if not the current version of java already)
-> javac *.java //7 class files should appear in directory
-> java Pager <machine-size> <page-size> <process-size> <job-mix> <number-of-references> <replacement-algorithm>
The first five arguments must be positive integers and last argument a string that is one of the following:
fifo, random, lru (caps insensitive)
-> It is expected that a file "random-numbers" will be in the working directory.
-> It is expected job mix will be values 1, 2, 3, or 4.

If successful, console will print the expected output for the input and replacement algorithm chosen.
NOTE: "The level of debugging..." statement is omitted from the output since there is no argument allowed for debugging.
If not successful, console will log the error that occurred.

Errors include:
-Number of arguments not equal to 6
-First five inputs are not integers
-Algorithm specified is not one of the above

***
Descriptions of Classes created:
-The FrameTable class is used to implement the different replacement algorithms for a Frame Table. The different classes
that are implemented as a FrameTable are: FIFO, RandomFT, and LRU.

-This Pager class is the main class used to run the program. It contains the main algorithm for finding page faults and
processing replacement using information specified in the spec.

-The Process class represents a Process in which its attributes will be modified based on page faults and replacement
algorithm.