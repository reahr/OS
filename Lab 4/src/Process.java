import java.util.Scanner;

/**
 * Reah Rajmangal, rr2886
 * Lab 4, 4/18/18
 * This class represents a Process in which its attributes will be modified based on page faults and replacement algorithm
 */
public class Process {
    int processId;
    int size;
    int numOfPageFaults;
    int totalResidencyTimes;
    int evictions;
    int nextWord;
    int references;

    public Process(int processId, int size) {
        this.processId = processId;
        this.size = size;
        numOfPageFaults = 0;
        totalResidencyTimes = 0;
        evictions = 0;
        this.nextWord = (111 * processId) % size;
        references = 0;
    }

    /**
     * This function sets the next Word reference for this process using probabilities given in spec
     *
     * @param random_numbers Scanner that reads the next Integer value from random-numbers file
     * @param A              value dependent on job mix (see Pager file for how A, B, C is determined)
     * @param B              value dependent on job mix
     * @param C              value dependent on job mix
     */
    public void nextWordReference(Scanner random_numbers, double A, double B, double C) {
        int random = random_numbers.nextInt();
        double y = random / (Integer.MAX_VALUE + 1d);
        //cases for calculating next references
        if (y < A) {
            nextWord = (nextWord + 1) % size; //prob A
        } else if (y < (A + B)) {
            nextWord = (nextWord - 5 + size) % size; //prob B
        } else if (y < A + B + C) {
            nextWord = (nextWord + 4) % size; //prob C
        } else {
            nextWord = random_numbers.nextInt() % size; //prob (1-A-B-C)/S
        }
    }
}

