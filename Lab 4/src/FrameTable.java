import java.util.ArrayList;
import java.util.Scanner;

/**
 * Reah Rajmangal, rr2886
 * Lab 4, 4/18/18
 * The FrameTable interface is used to implement the different replacement algorithms for a Frame Table
 */
public interface FrameTable {
    ArrayList<Page> frameTable = new ArrayList<>(); //will hold all pages/frames

    /**
     * This function checks if the frame table contains a Page with the Process ID and Page Number
     *
     * @param processID the process ID of the page desired
     * @param pageNum   the number of the page desired
     * @return a Page object with the ID desired
     */
    default Page containsPage(int processID, int pageNum) {
        for (int i = 0; i < frameTable.size(); i++) {
            Page p = frameTable.get(i);
            if (p.processID == processID && p.pageNum == pageNum) {
                return p; //we found the demand page
            }
        }
        return null; //null if we cannot find it
    }

    /**
     * This functions will be implemented based on the different replacement algorithms
     *
     * @param processes a list of processes in which an element will be modified based on the page replaced
     * @param processID process ID of page being replaced
     * @param pageNum   number of page being replaced
     * @param time      time recorded for replacing page
     */
    void replace(ArrayList<Process> processes, int processID, int pageNum, int time);
}

/**
 * The FIFO class implements the FIFO replacement algorithm for a frame table
 */
class FIFO implements FrameTable {
    int numOfFrames;

    FIFO(int numOfFrames) {
        this.numOfFrames = numOfFrames;
        //initialize all frames of frame table to null, set pid to 0 ok because we start from 1+
        for (int i = 0; i < numOfFrames; i++) {
            frameTable.add(i, new Page(0, 0, 0));
        }
    }

    /**
     * This is the replacement algorithm for First In, First Out
     *
     * @param processes a list of processes in which an element will be modified based on the page replaced
     * @param processID process ID of page being replaced
     * @param pageNum   number of page being replaced
     * @param time      time recorded for replacing page
     */
    public void replace(ArrayList<Process> processes, int processID, int pageNum, int time) {
        if (numOfFrames == frameTable.size()) {
            Page evicted = frameTable.get(0);
            Process evictedProcess = processes.get(evicted.processID - 1); //since offset of 1 for ArrayList
            evictedProcess.evictions++;
            evictedProcess.totalResidencyTimes += (time - evicted.loadTime);
            frameTable.remove(0); //remove first in 'queue'
        }
        frameTable.add(new Page(processID, pageNum, time));
    }
}

/**
 * The RandomFT class represents the Random replacement algorithm for a frame table
 */
class RandomFT implements FrameTable {
    int numOfFrames;
    Scanner random; //contains random-numbers

    RandomFT(int numOfFrames, Scanner random) {
        this.numOfFrames = numOfFrames;
        this.random = random;
        //initialize all frames of frame table to null, set pid to 0 ok because we start from 1+
        for (int i = 0; i < numOfFrames; i++) {
            frameTable.add(i, new Page(0, 0, 0));
        }
    }

    /**
     * Replacement algorithm for Random
     *
     * @param processes a list of processes in which an element will be modified based on the page replaced
     * @param processID process ID of page being replaced
     * @param pageNum   number of page being replaced
     * @param time      time recorded for replacing page
     */
    public void replace(ArrayList<Process> processes, int processID, int pageNum, int time) {
        for (int i = numOfFrames; i > 0; i--) {
            if (frameTable.get(i).processID == 0 && frameTable.get(i).pageNum == 0) {
                frameTable.set(i, new Page(processID, pageNum, time));
                return;
            }
        }

        int num = random.nextInt();
        Page evicted = frameTable.get(num % numOfFrames);
        Process evictedProcess = processes.get(evicted.processID - 1);
        evictedProcess.evictions++;
        evictedProcess.totalResidencyTimes += (time - evicted.loadTime);
        frameTable.set(num % numOfFrames, new Page(processID, pageNum, time));
    }
}

class LRU implements FrameTable {
    int numOfFrames;

    LRU(int numOfFrames) {
        this.numOfFrames = numOfFrames;
        //initialize all frames of frame table to null, set pid to 0 ok because we start from 1+
        for (int i = 0; i < numOfFrames; i++) {
            frameTable.add(i, new Page(0, 0, 0));
        }
    }

    /**
     * Replacement algorithm for Least Recently Used
     *
     * @param processes a list of processes in which an element will be modified based on the page replaced
     * @param processID process ID of page being replaced
     * @param pageNum   number of page being replaced
     * @param time      time recorded for replacing page
     */
    public void replace(ArrayList<Process> processes, int processID, int pageNum, int time) {
        int leastRecentUsed = time;
        Page replacement = null;
        int replacementIndex = 0;

        for (int i = numOfFrames - 1; i >= 0; i--) {
            Page indexPg = frameTable.get(i);
            if (indexPg.processID == 0 && indexPg.pageNum == 0) {
                Page p = new Page(processID, pageNum, time);
                p.timeLastModified = time; //set now to this time, since unused frame
                frameTable.set(i, p);
                return;
            } else if (leastRecentUsed > indexPg.timeLastModified) {
                replacementIndex = i;
                replacement = indexPg;
                leastRecentUsed = indexPg.timeLastModified;
            }
        }

        Process evictedProcess = processes.get(replacement.processID - 1);
        evictedProcess.evictions++;
        evictedProcess.totalResidencyTimes += (time - replacement.loadTime);

        //create new Page to be replaced and modify its LRT
        Page newPage = new Page(processID, pageNum, time);
        newPage.timeLastModified = time;
        frameTable.set(replacementIndex, newPage);
    }

}

/**
 * The Page class represents a page within a frame that is identified using primarily its ID and size
 */
class Page {
    int processID;
    int pageNum;
    int loadTime;
    int timeLastModified; //only used for LRU algo

    Page(int processID, int pageNum, int loadTime) {
        this.processID = processID;
        this.pageNum = pageNum;
        this.loadTime = loadTime;
    }
}
