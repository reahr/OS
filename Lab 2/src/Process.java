import java.util.ArrayList;
import java.util.Comparator;

/**
 * Reah Rajmangal, rr2886, Operating Systems Lab 2
 * The Process class creates an object that contains all information about the each process and modifies based
 * off its next job
 */
public class Process implements Comparable<Process>{

    int arrival_time, total_CPU_time, cpuBurst, cpuBurstTime, ioBurst, ioBurstTime;

    String state;

    Integer remainingCPUBurstTime=null;

    int sortInput; //to sort by Input

    int rem, waitingTime, ioTime, finishingTime;

    public Process (int a, int b, int c, int io){
        this.arrival_time=a;
        this.total_CPU_time=c;
        this.cpuBurst=b;
        this.ioBurst=io;
        ioTime=0;
        waitingTime=0;
        state="unstarted";
        rem=total_CPU_time;
    }

    /**
     * Decreases the remaining and CPU burst time (if applicable)
     */
    public void crunch(){
        cpuBurstTime--;
        rem--;
    }

    /**
     * Resets the process such that it is the same before any algorithm was called on it
     */
    public void reset(){
        ioTime=0;
        waitingTime=0;
        state="unstarted";
        rem=total_CPU_time;
        ioBurstTime=0;
        cpuBurstTime=0;
        remainingCPUBurstTime=null;
    }

    /**
     * Returns the turnaround time
     * @return an integer which is the turn around time for this Process
     */
    public int getTurnaround (){
        return finishingTime-arrival_time;
    }

    public int compareTo (Process p){
        int comparingProcess= ((Process) p).arrival_time;
        if (this.arrival_time - comparingProcess == 0){
            return this.sortInput- ((Process) p).sortInput;
        }
        return this.arrival_time - comparingProcess;
    }

    /**
     * Gives a String that shows the state of this process along with any other applicable information
     * @return A string that represents the state of this Process
     */
    public String printState(){
        if (this.state.equals("unstarted")) return "unstarted  0";

        else if (this.state.equals("terminated")) return "terminated 0";
        else if (this.state.equals("blocked")) {
            return String.format("blocked  %d", ioBurstTime);
        }
        else if (this.state.equals("ready")) {
            if (remainingCPUBurstTime !=null){
                return String.format("ready  %d", remainingCPUBurstTime);
            }
            return "ready  0";
        }

        else if (this.state.equals("running")){
            return String.format("running  %d", cpuBurstTime);
        }

        else return "Something went wrong.";
    }

    /**
     * @param quantum for Round Robin, overloading
     * @return A String that represents the state of this Process
     */
    public String printState(int quantum){
        if (this.state.equals("unstarted")) return "unstarted  0";

        else if (this.state.equals("terminated")) return "terminated 0";
        else if (this.state.equals("blocked")) {
            return String.format("blocked  %d", ioBurstTime);
        }
        else if (this.state.equals("ready")) {
            return "ready  0";
        }

        else if (this.state.equals("running")){
            if (cpuBurstTime==1){
                return String.format("running  %d", cpuBurstTime);
            }
            if (quantum==2){
                return String.format("running  %d", quantum);
            }else if (quantum==1){
                return String.format("running  %d", quantum);
            }
            return String.format("running  %d", cpuBurstTime);
        }

        else return "Something went wrong.";
    }

    /**
     * @return A String that represents the summary stats of this Process, called in each algorithm
     */
    public String toString(){
        return String.format("%d %d %d %d", arrival_time, cpuBurst, total_CPU_time, ioBurst);
    }

    public String printPSum(){
        return String.format (
                "   (A, B, C, IO) = (%d,%d,%d,%d)\n" +
                        "   Finishing time: %d\n" +
                        "   Turnaround time: %d\n" +
                        "   I/O time: %d\n" +
                        "   Waiting time: %d\n\n",
                arrival_time, cpuBurst, total_CPU_time, ioBurst,
                finishingTime,
                this.getTurnaround(),
                ioTime,
                waitingTime
        );
    }
}

/**
 * This class sorts Processes by remainder, this is used by the PSJF algorithm
 */
class SortByRemainder implements Comparator <Process>{
    public int compare (Process a, Process b){
        if (a.rem - b.rem==0){
            return a.sortInput- b.sortInput;
        }
        return a.rem - b.rem;
    }
}
