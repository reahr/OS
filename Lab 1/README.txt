Reah Rajmangal
Operating Systems Lab 1 README

The source code consists of three java files and requires Java 1.8.* in order to run.
TwoPassLinker.java is the main file that needs to run.

***
In the working directory (RR-LAB-1), use the following commands:
->javac *.java //3 class files should appear in directory
->java TwoPassLinker [input filename/path]

If successful, console will print the expected output.
If not successful, console will log the error that occurred (regarding formatting of input/file read).

Errors include: 
-Input file not found or could not be read
-General format of input file is not correct 
-Various formats are incorrect based off of the type of list: Definition, Use, or Program Text

***
Descriptions of Classes created:
-The Module class parses the file into according lists. Those lists are then checked for errors while  relocating relative addresses, and resolving external references.

-The Instruction class creates an Instruction that checks if Instruction is correct based on specification

-The Symbol class creates a symbol with definition and other properties that are altered if it is used or duplicated

Note: These classes are not private for the sake of accessing data fields immediately rather than calling methods. The classes are instead package-private by default.