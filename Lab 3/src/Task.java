import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.lang.Math;

/**
 * Reah Rajmangal, Lab 3
 * This class represents a Task which has multiple activities
 */

public class Task {
    int id;
    ArrayList<Integer> rscAlloc=new ArrayList<>(); //rsc's allocated for each type for task (based upon requests)
    int wait, terminated; //terminated is the cycle terminated at
    int state=0; //0 means safe, 1 means blocked
    ArrayList<Integer> claims= new ArrayList<>(); //new arraylist for multiple claims
    String status;

    Queue<Activity> activities = new LinkedList<>();

    public Task (int ID){
        this.id=ID;
        this.status=null; //we'll only set the status to "abort" if necessary
    }

    /**
     * String representation of task and all of its activities
     * @return String representation of task and all of its activities
     */
    @Override
    public String toString(){

        StringBuilder s=new StringBuilder();
       for (Activity a : activities){
           s.append(a.toString()+"\n");
       }
       return s.toString();
    }

    /**
     * String representation for final status of this Task
     * @return String representation for final status of this Task, including wait time and finishing time
     */
    public String taskOutput(){
        if (this.status !=null && this.status.equals("aborted")){
            return String.format("Task %2d    aborted", this.id);

        }
        return String.format("Task %2d   %2d   %2d   %2d%%", this.id, this.terminated, this.wait,
                (int)Math.round(100.0 / this.terminated * this.wait));
    }
}

/**
 * This class represents an activity a Task can have, serves as a parent class for each activity
 */
class Activity {
    int delay; //just grab the delay from the objects
}

/**
 * This class represents a Release activity
 */
class Release extends Activity{
    int resourceType;
    int amount;
    int taskID;

    public Release (int resourceType, int amount, int delay, int taskID){
        this.resourceType=resourceType;
        this.delay=delay;
        this.amount=amount;
        this.taskID=taskID;
    }

    @Override
    public String toString(){
        return String.format("Releasing %d amount of resource %d", amount, resourceType);
    }
}

/**
 * This class represents a Request activity
 */
class Request extends Activity{
    Resource rscType;
    int requestamt;
    int id; //id of rsc we want to request/allocate
    int taskID; //which task belonged to

    Request(Resource resource, int requestAmt, int delay, int taskID){
        this.rscType=resource;
        this.requestamt=requestAmt;
        this.delay=delay;
        this.id=resource.id;
        this.taskID=taskID;
    }

    /**
     * String representation of a Request activity
     * @return String representation of a Request activity
     */
    @Override
    public String toString(){
        return String.format("Requesting %d amount of resource %d", requestamt, rscType.id);
    }

    /**
     * Checks if another Request is equal to this Request, necessary for removing a request from blocked request lists
     * @param o Object Request that will be casted if accepted
     * @return true if equal
     */
    @Override
    public boolean equals (Object o){
        if (o instanceof Request){
            Request r= (Request) o;
            if (this.taskID==r.taskID && this.requestamt==r.requestamt && this.rscType == r.rscType &&
                    this.id==r.id) return true;
            return false;
        }
        return false;
    }
}

/**
 * This class represents a Initiate activity
 */
class Initialize extends Activity{
    int id; //task id
    Resource resource;
    int claim;

    public Initialize ( Resource resource, int amount){
        this.resource=resource;
        this.claim=amount;
    }

    /**
     * String representation of a Initiate activity
     * @return String representation of a Initiate activity
     */
    @Override
    public String toString(){
        return String.format("Initializing %d amount of resource %d to task %d", claim, resource.id, id);
    }
}

/**
 * This class represents a Terminate activity
 */
class Terminate extends Activity{
    int id; //task id so we know which task terminated

    public Terminate (int taskID, int delay){
        this.delay=delay;
        this.id=taskID;
    }

    /**
     * String representation of a Terminate activity
     * @return String representation of a Terminate activity
     */
    @Override
    public String toString(){
        return String.format("Terminating %d after delay %d", id, delay);
    }
}
