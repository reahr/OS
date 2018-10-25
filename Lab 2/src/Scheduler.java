import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Reah Rajmangal, rr2886, Operating Systems Lab 2
 * This is the main file used to run the Scheduler, see README for more information
 */
public class Scheduler {
    public static void main(String[] args) throws FileNotFoundException {

        if (args.length < 1) {
            System.err.printf("ERROR: the program expects a file name as an argument.\n");
            System.err.printf("Usage 1: java Scheduler [fileName]\n");
            System.err.printf("Usage 2: java Scheduler --verbose [fileName]\n");
            System.exit(1);
        }

        File file = null;
        String path="filename";
        boolean verbose = false;

        if (args.length == 1) {
            path = args[0];
            file = new File(path);
        } else if (args.length == 2) {
            if (!args[0].equals("--verbose")) {
                System.err.printf("ERROR: the program expects a '--verbose' as the first argument.\n");
                System.err.printf("Usage: java Scheduler --verbose [fileName]\n");
                System.exit(1);
            }
            verbose = true;
            path = args[1];
            file = new File(path);
        }

        if (!file.exists()) {
            System.err.printf("ERROR: file %s does not exist.\n", path);
            System.exit(1);
        }

        if (!file.canRead()) {
            System.err.printf("ERROR: file %s cannot be read.\n", path);
        }

        //check if file is empty based on size
        if (file.length() == 0) {
            System.err.printf("ERROR: %s is empty.", path);
            System.exit(1);
        }

        Scanner scanner = new Scanner(file);

        //parse processes into objects
        ArrayList<Process> processes = new ArrayList<>(scanner.nextInt());
        int sort = 0;
        while (scanner.hasNext()) {
            try {
                Process a = new Process(scanner.nextInt(), scanner.nextInt(), scanner.nextInt(), scanner.nextInt());
                a.sortInput = sort;
                sort++;
                processes.add(a);
            } catch (InputMismatchException e) {
                break;
            }
        }
        scanner.close();

        System.out.println("------------Logging First Come First Serve--------------\n");
        FCFS(processes, verbose);
        System.out.println("\n------------Logging Round Robin with Quantum 2----------\n");
        RRQ2(processes, verbose);
        System.out.println("\n-----------------Logging UniProcessor-------------------\n");
        UNI(processes, verbose);
        System.out.println("\n---------Logging Preemptive Shortest Job First----------\n");
        SRTN(processes, verbose);

    }

    public static void FCFS(ArrayList<Process> processes, boolean verbose) {
        int numOfProcessesTerm = 0;
        int total = processes.size();
        int cycle = 0;
        Process processRun = null;
        RandomBurst r = new RandomBurst();
        int totalFinish = 0;
        int sumTurnaround = 0;
        int sumWaitingTime = 0;
        int totalIO = 0;
        int totalCPU = 0;

        System.out.printf("The original input was: %d  ", processes.size());
        for (Process p : processes) {
            System.out.print(p.toString() + "  ");
        }
        System.out.println("");

        //first let's sort the processes based on arrival time:
        Collections.sort(processes);

        System.out.printf("The (sorted) input was: %d  ", processes.size());
        for (Process p : processes) {
            System.out.print(p.toString() + "  ");
        }

        if (verbose) System.out.println("\n\nThis detailed printout gives the state and remaining burst for each process\n");

        Queue<Process> readyProcesses = new LinkedList<>();
        ArrayList<Process> blockedProcesses = new ArrayList<>();

        while (numOfProcessesTerm < total) {
            //running processes
            if (processRun != null) {
                totalCPU++;
                processRun.crunch();
            }

            if (processRun != null) {
                if (processRun.rem == 0) {
                    processRun.state = "terminated";
                    processRun.finishingTime = cycle - 1;
                    numOfProcessesTerm++;
                    if (numOfProcessesTerm == total) {
                        totalFinish = processRun.finishingTime;
                        break;
                    }
                    processRun = null;
                } else if (processRun.cpuBurstTime == 0) {
                    processRun.ioBurstTime = r.randomOS(processRun.ioBurst);
//                    System.out.println("For IO burst: "+ r.num);
                    processRun.state = "blocked";
                    blockedProcesses.add(processRun);
                    processRun = null;
                }
            }

            //ready processes
            if (processRun == null) {
                if (!readyProcesses.isEmpty()) {
                    processRun = readyProcesses.remove();
                    processRun.state = "running";
                    int posCPUBurst = r.randomOS(processRun.cpuBurst);
//                    System.out.println("For CPU burst using: "+r.num + " " + posCPUBurst);
                    if (posCPUBurst > processRun.rem + 1) processRun.cpuBurstTime = processRun.rem + 1;
                    else {
                        processRun.cpuBurstTime = posCPUBurst;
                    }
                }
            }

            if (verbose){
                System.out.printf("Before cycle %d: ", cycle);
                for (Process p : processes) {
                    System.out.printf("%17s", p.printState());
                    if (p.state.equals("ready")) p.waitingTime++;
                }
                System.out.print(".");
                System.out.println("");
            }else {
                for (Process p : processes) {
                    if (p.state.equals("ready")) p.waitingTime++;
                }
            }

            //this is to make sure if blocked processes are ready at "same time",
            //we add to queue based off arrival/input
            ArrayList<Process> sortMe = new ArrayList<>();

            //blocked processes
            if (!blockedProcesses.isEmpty()) totalIO++;

            for (int i = 0; i < blockedProcesses.size(); i++) {
                Process p = blockedProcesses.get(i);
                p.ioTime++;
                p.ioBurstTime--;
                if (p.ioBurstTime == 0) {
                    sortMe.add(p);
                    p.state = "ready";
                    blockedProcesses.remove(i);
                    i--;
                }
            }

            Collections.sort(sortMe);

            for (Process p : sortMe) {
                readyProcesses.add(p);
            }

            //arriving processes
            for (int i = 0; i < processes.size(); i++) {
                if (processes.get(i).arrival_time == cycle) {
                    readyProcesses.add(processes.get(i));
                    processes.get(i).state = "ready";
                }
            }

            cycle++;
        }

        System.out.println("\nThe scheduling algorithm used was First Come First Served\n");

        for (int i = 0; i < processes.size(); i++) {
            System.out.printf("Process %d:\n", i);
            Process p = processes.get(i);
            sumTurnaround += p.getTurnaround();
            sumWaitingTime += p.waitingTime;
            System.out.print(p.printPSum());
            p.reset();
        }

        float avgTTime = (float) sumTurnaround / (processes.size());
        float avgWTime = (float) sumWaitingTime / (processes.size());
        float thruput = (float) processes.size() / totalFinish * 100;
        float cpuUtil = (float) totalCPU / totalFinish;
        float ioUtil = (float) totalIO / totalFinish;

        System.out.println("Summary Data:");
        System.out.printf("   Finishing time: %d\n" +
                        "   CPU Utilization: %f\n" +
                        "   I/O Utilization: %f\n" +
                        "   Throughput: %f processes per hundred cycles\n" +
                        "   Average turnaround time: %f\n" +
                        "   Average waiting time: %f\n",
                totalFinish, cpuUtil, ioUtil, thruput, avgTTime, avgWTime);

        r.close();
    }

    public static void RRQ2(ArrayList<Process> processes, boolean verbose) {
        int quantum = 2;
        int numOfProcessesTerm = 0;
        int total = processes.size();
        int cycle = 0;
        Process processRun = null;
        RandomBurst r = new RandomBurst();
        int totalFinish = 0;
        int sumTurnaround = 0;
        int sumWaitingTime = 0;
        int totalIO = 0;
        int totalCPU = 0;

        System.out.printf("The original input was: %d  ", processes.size());
        for (Process p : processes) {
            System.out.print(p.toString() + "  ");
        }
        System.out.println("");

        //first let's sort the processes based on arrival time:
        Collections.sort(processes);

        System.out.printf("The (sorted) input was: %d  ", processes.size());
        for (Process p : processes) {
            System.out.print(p.toString() + "  ");
        }

        if (verbose) System.out.println("\n\nThis detailed printout gives the state and remaining burst for each process\n");

        Queue<Process> readyProcesses = new LinkedList<>();
        ArrayList<Process> blockedProcesses = new ArrayList<>();

        while (numOfProcessesTerm < total) {
            //running processes
            if (processRun != null) {
                totalCPU++;
                processRun.crunch();
            }

            if (quantum <= 0) {
                if (processRun != null) {
                    if (processRun.rem == 0) {
                        processRun.state = "terminated";
                        processRun.finishingTime = cycle - 1;
                        numOfProcessesTerm++;
                        if (numOfProcessesTerm == total) {
                            totalFinish = processRun.finishingTime;
                            break;
                        }
                        processRun.remainingCPUBurstTime = null;
                        processRun = null;
                    } else if (processRun.cpuBurstTime == 0) {
                        processRun.ioBurstTime = r.randomOS(processRun.ioBurst);
//                        System.out.println("For IO burst: "+ r.num);
                        processRun.state = "blocked";
                        blockedProcesses.add(processRun);
                        processRun.remainingCPUBurstTime = null;
                        processRun = null;
                    } else {
                        processRun.state = "ready";
                        processRun.remainingCPUBurstTime = processRun.cpuBurstTime;
//                        System.out.println("my remaining burst time is: " + processRun.remainingCPUBurstTime);
                        processRun = null;
                    }
                }
            } else {
                if (processRun != null) {
                    if (processRun.rem == 0) {
                        processRun.state = "terminated";
                        processRun.finishingTime = cycle - 1;
                        numOfProcessesTerm++;
                        if (numOfProcessesTerm == total) {
                            totalFinish = processRun.finishingTime;
                            break;
                        }
                        processRun.remainingCPUBurstTime = null;
                        processRun = null;
                    } else if (processRun.cpuBurstTime == 0) {
                        processRun.ioBurstTime = r.randomOS(processRun.ioBurst);
//                        System.out.println("For IO burst: "+ processRun.ioBurstTime);
                        processRun.state = "blocked";
                        blockedProcesses.add(processRun);
                        processRun.remainingCPUBurstTime = null;
                        processRun = null;
                    }
                }
            }

            //ready processes
            if (processRun == null) {
                if (!readyProcesses.isEmpty()) {
                    processRun = readyProcesses.remove();
                    quantum = 2;
//                    System.out.println("Using this: "+ processRun.toString());
                    processRun.state = "running";
                    if (processRun.remainingCPUBurstTime != null)
                        processRun.cpuBurstTime = processRun.remainingCPUBurstTime;
                    else {
                        int posCPUBurst = r.randomOS(processRun.cpuBurst);
//                        System.out.println("For CPU burst using: " + r.num + " " + posCPUBurst);
                        processRun.cpuBurstTime = posCPUBurst;

                    }
                }
            }

            if (verbose) {
                System.out.printf("Before cycle %d: ", cycle);
                for (Process p : processes) {
                    System.out.printf("%17s", p.printState(quantum));
                    if (p.state.equals("ready")) p.waitingTime++;
                }
                System.out.print(".");
                System.out.println("");
            }else {
                for (Process p : processes) {
                    if (p.state.equals("ready")) p.waitingTime++;
                }
            }

            //this is to make sure if blocked processes are ready at "same time",
            //we add to queue based off arrival/input
            ArrayList<Process> sortMe = new ArrayList<>();

            if (!blockedProcesses.isEmpty()) totalIO++;

            //blocked processes
            for (int i = 0; i < blockedProcesses.size(); i++) {
                Process p = blockedProcesses.get(i);
                p.ioTime++;
                p.ioBurstTime--;
                if (p.ioBurstTime == 0) {
                    sortMe.add(p);
                    p.state = "ready";
                    blockedProcesses.remove(i);
                    i--;
                }
            }

            //premptive, we have to look for quantum ==0 if and add to ready queue with blocked
            if (processRun != null) {
                if (quantum - 1 == 0) {
                    if (processRun.cpuBurstTime - 1 > 0 && processRun.rem - 1 > 0) {
                        sortMe.add(processRun);
                    }
                }
            }

            Collections.sort(sortMe);

            for (Process p : sortMe) {
                readyProcesses.add(p);
            }


            //arriving processes
            for (int i = 0; i < processes.size(); i++) {
                if (processes.get(i).arrival_time == cycle) {
                    readyProcesses.add(processes.get(i));
                    processes.get(i).state = "ready";
                }
            }

            cycle++;
            quantum--;
        }

        System.out.println("\nThe scheduling algorithm used was Round Robin\n");

        for (int i = 0; i < processes.size(); i++) {
            System.out.printf("Process %d:\n", i);
            Process p = processes.get(i);
            sumTurnaround += p.getTurnaround();
            sumWaitingTime += p.waitingTime;
            System.out.print(p.printPSum());
            p.reset();
        }

        float avgTTime = (float) sumTurnaround / (processes.size());
        float avgWTime = (float) sumWaitingTime / (processes.size());
        float thruput = (float) processes.size() / totalFinish * 100;
        float cpuUtil = (float) totalCPU / totalFinish;
        float ioUtil = (float) totalIO / totalFinish;

        System.out.println("Summary Data:");
        System.out.printf("   Finishing time: %d\n" +
                        "   CPU Utilization: %f\n" +
                        "   I/O Utilization: %f\n" +
                        "   Throughput: %f processes per hundred cycles\n" +
                        "   Average turnaround time: %f\n" +
                        "   Average waiting time: %f\n",
                totalFinish, cpuUtil, ioUtil, thruput, avgTTime, avgWTime);

        r.close();
    }

    public static void SRTN(ArrayList<Process> processes, boolean verbose) {
        int numOfProcessesTerm = 0;
        int total = processes.size();
        int cycle = 0;
        Process processRun = null;
        RandomBurst r = new RandomBurst();
        int totalFinish = 0;
        int sumTurnaround = 0;
        int sumWaitingTime = 0;
        int totalCPU = 0;
        int totalIO = 0;

        System.out.printf("The original input was: %d  ", processes.size());
        for (Process p : processes) {
            System.out.print(p.toString() + "  ");
        }
        System.out.println("");

        //first let's sort the processes based on arrival time:
        Collections.sort(processes);

        System.out.printf("The (sorted) input was: %d  ", processes.size());
        for (Process p : processes) {
            System.out.print(p.toString() + "  ");
        }

        if (verbose) System.out.println("\n\nThis detailed printout gives the state and remaining burst for each process\n");

        ArrayList<Process> readyProcesses = new ArrayList<>();
        ArrayList<Process> blockedProcesses = new ArrayList<>();

        while (numOfProcessesTerm < total) {
            //running processes
            if (processRun != null) {
                totalCPU++;
                processRun.crunch();
            }

            if (processRun != null) {
                if (processRun.rem == 0) {
                    processRun.state = "terminated";
                    processRun.finishingTime = cycle - 1;
                    numOfProcessesTerm++;
                    if (numOfProcessesTerm == total) {
                        totalFinish = processRun.finishingTime;
                        break;
                    }
                    processRun.remainingCPUBurstTime = null;
                    processRun = null;
                } else if (processRun.cpuBurstTime == 0) {
                    processRun.ioBurstTime = r.randomOS(processRun.ioBurst);
//                    System.out.println("For IO burst: "+ r.num +" -- "+ processRun.ioBurstTime);
                    processRun.state = "blocked";
                    blockedProcesses.add(processRun);
                    processRun.remainingCPUBurstTime = null;
                    processRun = null;
                } else {
                    readyProcesses.add(processRun);
                    processRun.remainingCPUBurstTime = processRun.cpuBurstTime;
                    processRun.state = "ready";
                    processRun = null;
                }
            }

            //ready processes
            if (processRun == null) {
                if (!readyProcesses.isEmpty()) {
                    Collections.sort(readyProcesses, new SortByRemainder());
                    processRun = readyProcesses.remove(0);
                    processRun.state = "running";

                    if (processRun.remainingCPUBurstTime != null)
                        processRun.cpuBurstTime = processRun.remainingCPUBurstTime;
                    else {
                        int posCPUBurst = r.randomOS(processRun.cpuBurst);
                        processRun.cpuBurstTime = posCPUBurst;
//                        System.out.println("For CPU burst using: "+r.num + " -- " + posCPUBurst);
                    }
                }
            }

            if (verbose){
                System.out.printf("Before cycle %d: ", cycle);
                for (Process p : processes) {
                    System.out.printf("%17s", p.printState());
                    if (p.state.equals("ready")) p.waitingTime++;
                }
                System.out.print(".");
                System.out.println("");
            }else {
                for (Process p : processes) {
                    if (p.state.equals("ready")) p.waitingTime++;
                }
            }


            //this is to make sure if blocked processes are ready at "same time",
            //we add to queue based off arrival/input
            ArrayList<Process> sortMe = new ArrayList<>();

            if (!blockedProcesses.isEmpty()) totalIO++;

            //blocked processes
            for (int i = 0; i < blockedProcesses.size(); i++) {
                Process p = blockedProcesses.get(i);
                p.ioTime++;
                p.ioBurstTime--;
                if (p.ioBurstTime == 0) {
                    sortMe.add(p);
                    p.state = "ready";
                    blockedProcesses.remove(i);
                    i--;
                }
            }

            Collections.sort(sortMe);

            for (Process p : sortMe) {
                readyProcesses.add(p);
            }

            //arriving processes
            for (int i = 0; i < processes.size(); i++) {
                if (processes.get(i).arrival_time == cycle) {
                    readyProcesses.add(processes.get(i));
                    processes.get(i).state = "ready";
                }
            }

            cycle++;
        }

        System.out.println("\nThe scheduling algorithm used was Preemptive Shortest Job First\n");

        for (int i = 0; i < processes.size(); i++) {
            System.out.printf("Process %d:\n", i);
            Process p = processes.get(i);
            sumTurnaround += p.getTurnaround();
            sumWaitingTime += p.waitingTime;
            System.out.print(p.printPSum());
            p.reset();
        }

        float avgTTime = (float) sumTurnaround / (processes.size());
        float avgWTime = (float) sumWaitingTime / (processes.size());
        float thruput = (float) processes.size() / totalFinish * 100;
        float cpuUtil = (float) totalCPU / totalFinish;
        float ioUtil = (float) totalIO / totalFinish;

        System.out.println("Summary Data:");
        System.out.printf("   Finishing time: %d\n" +
                        "   CPU Utilization: %f\n" +
                        "   I/O Utilization: %f\n" +
                        "   Throughput: %f processes per hundred cycles\n" +
                        "   Average turnaround time: %f\n" +
                        "   Average waiting time: %f\n",
                totalFinish, cpuUtil, ioUtil, thruput, avgTTime, avgWTime);

        r.close();
    }

    public static void UNI(ArrayList<Process> processes, boolean verbose) {
        int numOfProcessesTerm = 0;
        int total = processes.size();
        int cycle = 0;
        Process processRun = null;
        RandomBurst r = new RandomBurst();
        int totalFinish = 0;
        int sumTurnaround = 0;
        int sumWaitingTime = 0;
        int totalIO = 0;
        int totalCPU = 0;

        System.out.printf("The original input was: %d  ", processes.size());
        for (Process p : processes) {
            System.out.print(p.toString() + "  ");
        }
        System.out.println("");

        //first let's sort the processes based on arrival time:
        Collections.sort(processes);

        System.out.printf("The (sorted) input was: %d  ", processes.size());
        for (Process p : processes) {
            System.out.print(p.toString() + "  ");
        }

        if (verbose) System.out.println("\n\nThis detailed printout gives the state and remaining burst for each process\n");

        LinkedList<Process> readyProcesses = new LinkedList<>();
        ArrayList<Process> blockedProcesses = new ArrayList<>();
        boolean uni = false;

        while (numOfProcessesTerm < total) {

            //running processes
            if (processRun != null) {
                totalCPU++;
                processRun.crunch();
            }

            if (processRun != null) {
                if (processRun.rem == 0) {
                    processRun.state = "terminated";
                    processRun.finishingTime = cycle - 1;
                    numOfProcessesTerm++;
                    if (numOfProcessesTerm == total) {
                        totalFinish = processRun.finishingTime;
                        break;
                    }
                    processRun = null;
                } else if (processRun.cpuBurstTime == 0) {
                    processRun.ioBurstTime = r.randomOS(processRun.ioBurst);
//                    System.out.println("For IO burst: "+ r.num + "   " + processRun.ioBurstTime);
                    processRun.state = "blocked";
                    blockedProcesses.add(processRun);
                    readyProcesses.addFirst(processRun);
                    //to ensure that this is the next process to run again
                    uni = true;
                    processRun = null;
                }
            }

            //ready processes
            if (processRun == null) {
                if (!uni) {
                    if (!readyProcesses.isEmpty()) {
                        processRun = readyProcesses.remove();
                        processRun.state = "running";
                        int posCPUBurst = r.randomOS(processRun.cpuBurst);
//                    System.out.println("For CPU burst using: "+r.num + " " + posCPUBurst);
                        processRun.cpuBurstTime = posCPUBurst;

                    }
                }
            }

            if (verbose) {
                System.out.printf("Before cycle %d: ", cycle);
                for (Process p : processes) {
                    System.out.printf("%17s", p.printState());
                    if (p.state.equals("ready")) p.waitingTime++;
                }
                System.out.print(".");
                System.out.println("");
            }else {
                for (Process p : processes) {
                    if (p.state.equals("ready")) p.waitingTime++;
                }
            }

            //blocked processes
            if (!blockedProcesses.isEmpty()) totalIO++;

            for (int i = 0; i < blockedProcesses.size(); i++) {
                Process p = blockedProcesses.get(i);
                p.ioTime++;
                p.ioBurstTime--;
                if (p.ioBurstTime == 0) {
                    blockedProcesses.remove(i);
                    uni = false;
                }
            }

            //arriving processes
            for (int i = 0; i < processes.size(); i++) {
                if (processes.get(i).arrival_time == cycle) {
                    readyProcesses.add(processes.get(i));
                    processes.get(i).state = "ready";
                }
            }

            cycle++;
        }

        System.out.println("\nThe scheduling algorithm used was Uniprocessor\n");

        for (int i = 0; i < processes.size(); i++) {
            System.out.printf("Process %d:\n", i);
            Process p = processes.get(i);
            sumTurnaround += p.getTurnaround();
            sumWaitingTime += p.waitingTime;
            System.out.print(p.printPSum());
            p.reset();
        }

        float avgTTime = (float) sumTurnaround / (processes.size());
        float avgWTime = (float) sumWaitingTime / (processes.size());
        float thruput = (float) processes.size() / totalFinish * 100;
        float cpuUtil = (float) totalCPU / totalFinish;
        float ioUtil = (float) totalIO / totalFinish;

        System.out.println("Summary Data:");
        System.out.printf("   Finishing time: %d\n" +
                        "   CPU Utilization: %f\n" +
                        "   I/O Utilization: %f\n" +
                        "   Throughput: %f processes per hundred cycles\n" +
                        "   Average turnaround time: %f\n" +
                        "   Average waiting time: %f\n",
                totalFinish, cpuUtil, ioUtil, thruput, avgTTime, avgWTime);

        r.close();
    }
}
