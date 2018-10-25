import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Reah Rajmangal, rr2886, Operating Systems Lab 2
 * This class opens and closes random-numbers file for each algorithm's call
 */
public class RandomBurst {

    File f;
    Scanner s;
    int num;

    public RandomBurst (){
        this.f=new File ("random-numbers");
        try{
            this.s=new Scanner(this.f);
        }catch (FileNotFoundException e){
            System.err.println("File 'random-numbers' cannot be found.");
        }

    }

    /**
     * This is the main function that is used to determine the IO or CPU burst, it reads the next integer from
     * the file random-numbers which is in working directory.
     * @param U CPU or IO burst determined by Process object
     * @return an integer determine by formula given
     */
    public int randomOS(int U){
        num=s.nextInt();
        return 1+(num%U);
    }

    /**
     * Closes the file, called at the end of each algorithm in Scheduler.java
     */
    public void close () {
        s.close();
    }
}
