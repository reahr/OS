import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Reah Rajmangal, rr2886
 * Lab 4, 4/18/18
 * This class is the main class used to run the program. See README for more information.
 */
public class Pager {
    public static void main(String[] args) throws FileNotFoundException {
        if (args.length != 6) {
            System.err.printf("ERROR: the program expects 6 arguments.\n");
            System.err.printf("Usage: java Pager <integer: machine size> <integer: page size> <integer: process size> <integer: job mix> " +
                    "<integer: number of references> <string: algorithm lru, fifo, random> \n");
            System.exit(1);
        }

        //todo make note "random-numbers" should be in directory

        String frameTableAlgo = args[5].trim().toLowerCase();

        if (!frameTableAlgo.equals("fifo") && !frameTableAlgo.equals("lru") && !frameTableAlgo.equals("random")) {
            System.err.printf("ERROR: algorithm " + frameTableAlgo + " is not a valid algorithm. " +
                    "Algorithms: lru, fifo, or random.\n");
            System.exit(1);
        }

        int machine_size = Integer.parseInt(args[0]);
        int page_size = Integer.parseInt(args[1]);
        int process_size = Integer.parseInt(args[2]);
        int job_mix = Integer.parseInt(args[3]);
        int numberOfReferences = Integer.parseInt(args[4]);

        Scanner scanner = new Scanner(new File("random-numbers"));

        int quantum = 3;
        int numOfFrames = machine_size / page_size;
        ArrayList<Process> processes = new ArrayList<>();

        FrameTable frameTable;
        if (frameTableAlgo.equals("fifo")) {
            frameTable = new FIFO(numOfFrames);
        } else if (frameTableAlgo.equals("random")) {
            frameTable = new RandomFT(numOfFrames, scanner);
        } else { //ok because initial check for one of the 3 algorithms
            frameTable = new LRU(numOfFrames);
        }

        //set values of A, B, and C accordingly based on spec
        double A[] = new double[4], B[] = new double[4], C[] = new double[4];
        if (job_mix == 1) { //will only access 1 element of each array though
            Arrays.fill(A, 1);
            Arrays.fill(B, 0);
            Arrays.fill(C, 0);
        } else if (job_mix == 2) {
            Arrays.fill(A, 1);
            Arrays.fill(B, 0);
            Arrays.fill(C, 0);
        } else if (job_mix == 3) {
            Arrays.fill(A, 0);
            Arrays.fill(B, 0);
            Arrays.fill(C, 0);
        } else if (job_mix == 4) {
            A[0] = 0.75;
            A[1] = 0.75;
            A[2] = 0.75;
            A[3] = 0.5;
            B[0] = 0.25;
            B[1] = 0.0;
            B[2] = 0.125;
            B[3] = 0.125;
            C[0] = 0;
            C[1] = 0.25;
            C[2] = 0.125;
            C[3] = 0.125;
        }

        //begin main run
        if (job_mix == 1) {
            //one process only
            processes.add(new Process(1, process_size));
            for (int i = 1; i <= numberOfReferences; i++) { //using <= since we start at 1 & need to include last ref
                int pageNum = processes.get(0).nextWord / page_size;
                Page pg = frameTable.containsPage(1, pageNum);
                if (pg == null) {
                    processes.get(0).numOfPageFaults++;
                    frameTable.replace(processes, 1, pageNum, i);
                } else {
                    //only used for lru, set p least recent time to time now
                    pg.timeLastModified = i;
                }

                processes.get(0).nextWordReference(scanner, A[0], B[0], C[0]);
            }
        } else {
            //initialize all processes (four)
            for (int i = 0; i < 4; i++) {
                processes.add(new Process(i + 1, process_size));
            }

            //each process will reference at most 3 times in a cycle
            int time = 0;
            int referenced = 0;
            int totalReferenced = numberOfReferences * 4;
            while (referenced < totalReferenced) {
                if ((totalReferenced - referenced) / 4 < 3) {
                    quantum = (totalReferenced - referenced) / 4; //remaining references per process
                }

                for (int i = 0; i < 4; i++) {
                    for (int j = 0; j < quantum; j++) {
                        time++;
//                        System.out.println(time + "\n");
                        referenced++;
//                        System.out.println("total referenced: "+referenced+"\n");
                        Process p = processes.get(i);
                        int pageNum = processes.get(i).nextWord / page_size;
                        Page pg = frameTable.containsPage(p.processId, pageNum);
                        if (pg == null) {
                            processes.get(p.processId - 1).numOfPageFaults++;
                            frameTable.replace(processes, p.processId, pageNum, time);
                        } else {
                            //only used for lru, set p least recent time to time now
                            pg.timeLastModified = time;
                        }
                        processes.get(i).nextWordReference(scanner, A[i], B[i], C[i]);
                    }
                }
            }
        }


        //begin output to screen
        int totalFaultTime = 0, totalResidencyTime = 0, totalEvictions = 0;

        System.out.printf("The machine size is %d.\n", machine_size);
        System.out.printf("The page size %d.\n", page_size);
        System.out.printf("The process size is %d.\n", process_size);
        System.out.printf("The job mix number is %d.\n", job_mix);
        System.out.printf("The number of references per process is %d.\n", numberOfReferences);
        System.out.printf("The replacement algorithm is %s.\n\n", frameTableAlgo);

        for (int i = 0; i < processes.size(); i++) {
            Process p = processes.get(i);
            totalFaultTime += p.numOfPageFaults;
            totalResidencyTime += p.totalResidencyTimes;
            totalEvictions += p.evictions;

            if (p.evictions == 0) {
                System.out.printf("Process %d has %d faults.\n\tWith no evictions, the average residence is undefined.\n",
                        p.processId, p.numOfPageFaults);
            } else {
                double avgResidency = (double) p.totalResidencyTimes / p.evictions;
                System.out.printf("Process %d has %d faults and %.1f average residency.\n", p.processId, p.numOfPageFaults,
                        avgResidency);
            }
        }
        System.out.println("");
        if (totalEvictions == 0) {
            System.out.printf("The total number of faults is %d.\n\tWith no evictions, the overall residency is undefined.",
                    totalFaultTime);
        } else {
            double totalAvgResidency = (double) totalResidencyTime / totalEvictions;
            System.out.printf("The total number of faults is %d and the overall average residency is %.1f.", totalFaultTime, totalAvgResidency);
        }
    }
}
