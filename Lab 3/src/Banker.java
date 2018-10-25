import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

/**
 * Reah Rajmangal, Lab 3
 * This class is the main file to run both FIFO and Banker's algorithm for a given input
 */

public class Banker {
    public static void main(String[] args) throws FileNotFoundException{
        if (args.length < 1) {
            System.err.printf("ERROR: the program expects a file name as an argument.\n");
            System.err.printf("Usage 1: java Banker [fileName]\n");
            System.exit(1);
        }

        File file = null;
        String path="filename";

        if (args.length == 1) {
            path = args[0];
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

        //parse first "line"
        int numOfTasks= Integer.parseInt(scanner.next());
        ArrayList<Resource> resources= new ArrayList<>();
        int numOfResources=scanner.nextInt();
        resources.add(null);

        for (int i=0; i < numOfResources; i++){ //start from i=2 since that is the first resource
            Resource r= new Resource(i+1, scanner.nextInt());
            resources.add(i+1,r);
        }

        //create two copies of each task, one for banker and one for fifo
        ArrayList<Task> tasks= new ArrayList<>();
        ArrayList<Task> tasksBanker= new ArrayList<>();
        ArrayList<String> BankerMessages= new ArrayList<>();

        for (int i=0; i < numOfTasks; i++){
            Task t1= new Task(i+1);
            tasks.add (t1); //we have to deal with offset of 1
            Task t2= new Task(i+1);
            tasksBanker.add(t2);
        }

        //set all claims each task's resources to 0
        for (int i=0; i < tasks.size(); i++){
            for (int j=0; j < resources.size(); j++) {
                tasks.get(i).claims.add(0);
                tasksBanker.get(i).claims.add(0);
            }
        }

        int cycle=0;
        int numTerm=0;
        int numTermBanker=0;

        //begin parsing
        while (scanner.hasNext()){
            String activity= scanner.next();
            if (activity.equals("initiate")){
                int taskNumber= Integer.parseInt(scanner.next()); //task id
                scanner.next(); //ignore delay for initiate
                int rscType=Integer.parseInt(scanner.next());
                int initialClaim= Integer.parseInt(scanner.next()); //initial claim
                //checking for safety before running
                if (initialClaim > resources.get(rscType).amount) {
                    tasksBanker.get(taskNumber-1).status="aborted";
                    numTermBanker++;
                    tasksBanker.get(taskNumber-1).terminated=cycle;
                    BankerMessages.add("Banker aborts task "+taskNumber+" before run begins:\n" +
                            "       claim for resourse "+rscType+" ("+initialClaim+") exceeds number of units present ("+resources.get(rscType).amount+")");
                }
                //for easier implementation, we'll set the tasks activities and claims etc
                //but will never be considered for banker's algorithm
                //add initialization activity for task
                tasks.get(taskNumber-1).activities.add(new Initialize(resources.get(rscType),initialClaim ));
                tasksBanker.get(taskNumber-1).activities.add(new Initialize(resources.get(rscType),initialClaim ));
                tasks.get(taskNumber-1).claims.set(rscType,initialClaim);
                tasksBanker.get(taskNumber-1).claims.set(rscType,initialClaim);
            }else if (activity.equals("request")){
                int taskNumber= Integer.parseInt(scanner.next()); //task id
                int delay= Integer.parseInt(scanner.next());
                int rscType=Integer.parseInt(scanner.next());
                int amt= Integer.parseInt(scanner.next()); //num of rsc type requested
                tasks.get(taskNumber-1).activities.add(new Request(resources.get(rscType),amt, delay, taskNumber));
                tasksBanker.get(taskNumber-1).activities.add(new Request(resources.get(rscType),amt, delay, taskNumber));
            }else if (activity.equals("release")){
                int taskNumber= Integer.parseInt(scanner.next()); //task id
                int delay= Integer.parseInt(scanner.next());
                int rscType=Integer.parseInt(scanner.next()); //num of rsc type requested
                int amt= Integer.parseInt(scanner.next()); //num releasing
                tasks.get(taskNumber-1).activities.add(new Release(rscType, amt, delay, taskNumber));
                tasksBanker.get(taskNumber-1).activities.add(new Release(rscType, amt, delay, taskNumber));
            }else if (activity.equals("terminate")){
                int taskNumber= Integer.parseInt(scanner.next()); //task id
                int delay= Integer.parseInt(scanner.next());
                scanner.next();//ignore
                scanner.next(); //ignore
                tasks.get(taskNumber-1).activities.add(new Terminate(taskNumber, delay));
                tasksBanker.get(taskNumber-1).activities.add(new Terminate(taskNumber, delay));
            }
        }

        //set all allocations of rsr type for each task to 0 (what resource and amounts task actually has)
        for (int i=0; i < tasks.size(); i++){
            for (int j=0; j < resources.size(); j++) {
                tasks.get(i).rscAlloc.add(0);
                tasksBanker.get(i).rscAlloc.add(0);
            }
        }

        //---------------------------------------------Begin Processing----------------------------------------------------
        fifo(numOfTasks, cycle, numTerm, tasks, resources);
        banker(numOfTasks, cycle, numTermBanker, tasksBanker, resources, BankerMessages);

        int totalFIFOTerm=0, totalFIFOWait=0;
        int totalBankerWait=0, totalBankerTerm=0;

        //print any errors from Banker re initial claims or request amounts > claims
        for (int i=0; i < BankerMessages.size(); i++){
            System.out.println(BankerMessages.get(i));
        }

        System.out.println("        FIFO                         BANKER'S      ");
        for (int i=0; i < tasks.size(); i++){
            Task fifo=tasks.get(i);
            Task banker=tasksBanker.get(i);
            System.out.printf("%-30s%-30s\n", fifo.taskOutput(),banker.taskOutput());
            //not counting aborted tasks
            if  (fifo.status == null) {
                totalFIFOTerm+=fifo.terminated;
                totalFIFOWait+=fifo.wait;
            }
            if (banker.status ==null) {
                totalBankerTerm += banker.terminated;
                totalBankerWait += banker.wait;
            }
        }
        String s1= String.format("Total     %2d   %2d   %2d%%", totalFIFOTerm, totalFIFOWait,
                (int)Math.round(100.0 / totalFIFOTerm * totalFIFOWait));
        String s2= String.format("Total     %2d   %2d   %2d%%", totalBankerTerm, totalBankerWait,
                (int)Math.round(100.0 / totalBankerTerm * totalBankerWait));
        System.out.printf("%-30s%-30s\n", s1, s2);
    }

    /**
     * This function performs the FIFO algorithm (optimistic resource manager)
     * @param numOfTasks number of tasks to be running
     * @param cycle time algorithm begins (0)
     * @param numTerm number of tasks already terminated pre running
     * @param tasks a list of Task objects that will be modified based on execution
     * @param resources list of Resource objects that will be checked against and modified for availability in subsequent cycles
     * @return a list of Task objects will final wait and termination cycles
     */
    public static ArrayList<Task> fifo(int numOfTasks, int cycle, int numTerm,
                                       ArrayList<Task> tasks, ArrayList<Resource> resources){

        ArrayList<Request> blockedRequests = new ArrayList<>();
        Queue<Release> releases = new LinkedList<>();
        int set= -1;
        int block=0; //check if num of blocked requests == non-terminated cycle
        int running=numOfTasks;

        while (numTerm < numOfTasks){
            //first check blocked requests and see if we can grant them
            for (int i=0; i < blockedRequests.size(); i++){
                Request b=blockedRequests.get(i);
                int resource_type= b.id;
                int taskID= b.taskID-1; //we are working with an offset of 1
                Task t=tasks.get(taskID); //get the task of this request
                if (!checkGrantedFIFO(b, resources)){
                    //remain in blocked queue
                    tasks.get(taskID).wait++;
                }else{
                    //can get resources now, release from blocked tasks
                    resources.get(resource_type).amount-= b.requestamt;
                    //add to original value in the rsc allocated array for this task
                    int orig= tasks.get(taskID).rscAlloc.get(resource_type);
                    tasks.get(taskID).rscAlloc.set(resource_type, orig+b.requestamt);
                    set=taskID; //setter is used to avoid double activity for specific task
                    blockedRequests.remove(i);
                    i--;
                    block--;
                }
            }

            //then check the non blocking tasks
            for (int i=0; i < tasks.size(); i++){
                Task t=tasks.get(i);
                if (t.status !=null && t.status.equals("aborted")) continue; //go on to the next task since this is not valid
                if (t.state==1) continue; //go to the next task, since we cannot fulfill any of this task's other activities atm
                if (tasks.get(i).activities.isEmpty()) continue; //go to next task, no activities to fulfill
                Activity peak= tasks.get(i).activities.peek();
                //checking delay
                if (!(peak instanceof Initialize) && peak.delay > 0){
                    peak.delay--;
                    continue;
                }
                Activity a= tasks.get(i).activities.remove();

                //initialize all claims
                if (a instanceof Initialize){ } //do nothing

                //checking if request granted or not, based solely off of available resource amounts
                else if (a instanceof Request){
                    Request r= (Request) a;
                    int resource_type=r.id;

                    //we must check if granted or not
                    if (!checkGrantedFIFO(r, resources)){
                        //add request to blocked queue
                        blockedRequests.add(r);
                        t.wait++;
                        t.state=1;
                        block++; //we'll add it to blocked number of tasks
                    }else{
                        //decrease amount of available amount for resource requested
                        resources.get(resource_type).amount-= r.requestamt;
                        //add to original value in the rsc allocated array for this task
                        int orig= t.rscAlloc.get(resource_type);
                        t.rscAlloc.set(resource_type, orig+r.requestamt);
                    }
                } else if (a instanceof Release){
                    //add into the queue that will add back number of resources deallocated in the cycle
                    //only AFTER all tasks have been checked
                    releases.add((Release) a);
                } else if (a instanceof Terminate){
                    //terminate cycle, decrease amount of running tasks
                    numTerm++;
                    t.terminated=cycle;
                    running--;
                }
            }

            //now we are going to release any resources that need to be released (however they will not be avail till next cycle)
            for (int i=0; i < releases.size(); i++){
                Release r= releases.remove();
                //add back to the resources
                int rType=r.resourceType;
                int amount= r.amount;
                resources.get(rType).amount+=amount;

                //subtract back from allocation within task (deallocate)
                int orig= tasks.get(r.taskID-1).rscAlloc.get(rType);
                tasks.get(r.taskID-1).rscAlloc.set(rType, orig-amount);
            }

            //check if deadlock
            if (block == running && running !=1 && running !=0){
                boolean deadlocked=true;
                while (deadlocked){
                    Request removed= getMinTaskDeadlocked(blockedRequests); //we'll remove first blocked task (based on spec)
                    blockedRequests.remove(removed);
                    //abort task for this request
                    Task t=tasks.get(removed.taskID-1);
                    t.status="aborted";
                    t.terminated=cycle;
                    numTerm++;
                    running--;
                    block--;
                    releaseAll(t, resources);
                    //we still have to check if there is a remaining deadlock
                    for (Request b : blockedRequests){
                        //this will change the deadlock to false if at least one of the remaining blocked req can be granted
                        deadlocked=!checkGrantedFIFO(b, resources);
                        if (!deadlocked) break;
                    }
                }
            }

            //have a setter that checks if request granted while in block, to prevent task double activity in one cycle
            if (set != -1){
                tasks.get(set).state=0; //unblock it
                set=-1;
            }

            cycle++;
        }

        return tasks;
    };

    /**
     * This function performs the Banker's algorithm which checks for safety when allocating resources
     * @param numOfTasks number of tasks to be running
     * @param cycle time algorithm begins (0)
     * @param numTermBanker number of tasks already terminated pre running
     * @param tasksBanker a list of Task objects that will be modified based on execution
     * @param resources list of Resource objects that will be checked against and modified for availability in subsequent cycles
     * @param messages a list which will have any error messages during running
     * @return a list of Task objects will final wait and termination cycles
     */
    public static ArrayList<Task> banker(int numOfTasks, int cycle, int numTermBanker, ArrayList<Task> tasksBanker,
                                         ArrayList<Resource> resources, ArrayList<String> messages){
        ArrayList<Request> blockedRequests = new ArrayList<>();
        Queue<Release> releases = new LinkedList<>();
        int set= -1;

        while (numTermBanker < numOfTasks){
            //first check for blocked tasks
            for (int i=0; i < blockedRequests.size(); i++){
                Request b=blockedRequests.get(i);
                int resource_type= b.id;
                int taskID= b.taskID-1; //we are working with an offset of 1
                Task t=tasksBanker.get(taskID); //get the task of this request
                if (!checkGranted(t, resources)){
                    //remain in blocked queue
                    tasksBanker.get(taskID).wait++;
                }else{
                    //can get resources now, release blocked task
                    resources.get(resource_type).amount-= b.requestamt;
                    //add to original value in the rsc allocated array for this task
                    int orig= tasksBanker.get(taskID).rscAlloc.get(resource_type);
                    tasksBanker.get(taskID).rscAlloc.set(resource_type, orig+b.requestamt);
                    set=taskID; //to prevent double activity
                    blockedRequests.remove(i);
                    i--;
                }
            }

            //then check remaining tasks
            for (int i=0; i < tasksBanker.size(); i++){
                Task t=tasksBanker.get(i);
                if (t.status !=null && t.status.equals("aborted")) continue; //go on to the next task since this is not valid
                if (t.state==1) continue; //go to the next task, since we cannot fulfill any of this task's other activities atm
                if (tasksBanker.get(i).activities.isEmpty()) continue; //go to next task since no more activities for this task
                Activity peak= tasksBanker.get(i).activities.peek();
                //if delay continue, not ready yet
                if (!(peak instanceof Initialize) && peak.delay > 0){
                    peak.delay--;
                    continue;
                }
                Activity a= tasksBanker.get(i).activities.remove();

                //initialize all claims
                if (a instanceof Initialize){ } //do nothing

                //checking if request granted or not
                else if (a instanceof Request){
                    Request r= (Request) a;
                    int resource_type=r.id;

                    //Safety--first we must check if the request amount is > claim - resources allocated (bc error)
                    if (r.requestamt > t.claims.get(resource_type)-t.rscAlloc.get(resource_type)){
                        t.status="aborted";
                        t.terminated=cycle;
                        numTermBanker++;
                        int released=releaseAll(t, resources);
                        messages.add("During cycle "+cycle+"-"+(cycle+1)+" of Banker's algorithms" +
                                ", Task "+t.id+"'s request exceeds its claim; aborted; "+released+" units available next cycle");
                        continue;
                    }
                    //we must check if granted or not (based off claims and resources allocated already--safety
                    if (!checkGranted(t, resources)){
                        //add request to blocked queue
                        blockedRequests.add(r);
                        t.wait++;
                        t.state=1;
                    }else{
                        resources.get(resource_type).amount-= r.requestamt;
                        //add to original value in the rsc allocated array for this task
                        int orig= t.rscAlloc.get(resource_type);
                        t.rscAlloc.set(resource_type, orig+r.requestamt);
                    }
                } else if (a instanceof Release){
                    //add into the queue that will add back number of resources deallocated in the cycle
                    //only AFTER all tasks have been checked
                    releases.add((Release) a);
                } else if (a instanceof Terminate){
                    numTermBanker++;
                    t.terminated=cycle;
                }
            }

            //release resources that any task no longer needs
            for (int i=0; i < releases.size(); i++){
                Release r= releases.remove();
                //add back to the resources
                int rType=r.resourceType;
                int amount= r.amount;
                resources.get(rType).amount+=amount;

                //subtract back from allocation within task (deallocate)
                int orig= tasksBanker.get(r.taskID-1).rscAlloc.get(rType);
                tasksBanker.get(r.taskID-1).rscAlloc.set(rType, orig-amount);
            }

            //have a setter that checks if request granted while in block
            //to prevent double activity for one task in a single cycle
            if (set != -1){
                tasksBanker.get(set).state=0; //unblock it
                set=-1;
            }

            cycle++;
        }

        return tasksBanker;
    }

    /**
     * Checks if a request is granted in FIFO algorithm for a task
     * @param r Request which will be checked if granted or not
     * @param resources list of Resource objects to check against for availability
     * @return true if the request is granted
     */
    public static boolean checkGrantedFIFO(Request r, ArrayList<Resource> resources){
        int resource_type= r.id;
        if (r.requestamt > resources.get(resource_type).amount) return false;
        return true;
    }

    /**
     * Checks if a request is granted in Banker's algorithm for a task
     * @param t Task will be checked to see if their resource allocation and claims are safe
     * @param listR list of Resource objects to check against for availability
     * @return true if this request is granted
     */
    public static boolean checkGranted(Task t, ArrayList<Resource> listR){
        //necessary to check safety using all resources task wants and availability
        for (int i=1; i < listR.size(); i++){
            if (t.claims.get(i)-t.rscAlloc.get(i) > listR.get(i).amount){
                return false;
            }
        }
        return true;
    }

    /**
     * Releases all resources allocated for a specific task
     * @param t Task whose resources we want to free
     * @param listR list of Resource objects will be modified to add on to amount of resources released
     * @return the amount of resources released by this task
     */
    public static int releaseAll(Task t, ArrayList<Resource> listR){
        int amountRelease=0;
        for (int i=1; i< listR.size(); i++){
            amountRelease+=listR.get(i).amount;
            listR.get(i).amount+=t.rscAlloc.get(i);
            t.rscAlloc.set(i, 0);
        }
        return amountRelease;
    }

    /**
     * Gets the lowest numbered deadlocked task (for FIFO)
     * @param blockedRequests list of blocked requests
     * @return Request whose owner (Task object) will be aborted
     */
    public static Request getMinTaskDeadlocked (ArrayList<Request> blockedRequests) {
        Request lowestDeadlockedTask=blockedRequests.get(0);
        int min=blockedRequests.get(0).taskID;
        for (int i=0; i < blockedRequests.size(); i++){
            if (blockedRequests.get(i).taskID < min){
                min=blockedRequests.get(i).taskID; //this is the lowest number task now
                lowestDeadlockedTask=blockedRequests.get(i);
            }
        }
        return lowestDeadlockedTask;
    }
}
