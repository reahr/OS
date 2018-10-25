Reah Rajmangal
Operating Systems Lab 3 README

The source code consists of three java files and requires Java 1.8.* in order to run.
Banker.java is the main file that needs to run.

***
In the working directory (RR-LAB-3), use the following commands:
-> javac *.java //8 class files should appear in directory 
-> java Banker <input-filename>

If successful, console will print the expected outputs for the two algorithms SIDE BY SIDE.
If not successful, console will log the error that occurred (regarding file read).

Errors include:
-Input file not found or could not be read

***
Descriptions of Classes created:
-The Banker class which is the main file that runs both FIFO and Banker's algorithm and logs results to screen

-The Task class represents a Task which has multiple activities

-The Activity class which consist of subclasses: Initiate, Request, Release, Terminate; represents
the possible activities of any task

-The Resource class which represents a resource and its amount at any given time

Note 1: These classes are not private for the sake of accessing data fields immediately rather than calling methods.
The classes are instead package-private by default.
There are no individual classes for each algorithm, in order to make calling each algorithm easier.

Note 2: It is expected that inputs are all valid and follow the format specified in the spec (freeform).