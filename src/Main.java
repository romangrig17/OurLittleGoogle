import View.GUI;
import ViewModel.Manager;

import java.awt.image.ImageProducer;
import java.util.*;

public class Main {

    public static void main(String[] args)
    {
        System.out.println("Hello World!");

        long start = System.currentTimeMillis();
        //GUI gui = new GUI();

        boolean stemming = false;
        Manager manager = new Manager("C:\\Users\\user1\\Desktop\\masters\\השלמה\\information_retrieval\\corpus\\corpus\\corpus","C:\\Users\\user1\\Desktop\\masters\\השלמה\\information_retrieval\\corpus\\corpus\\write",stemming);


        long elapsedTime = System.currentTimeMillis() - start;
        System.out.println("The time of program: " + (elapsedTime/60000) + " Min");
    }
    
    
    

}
    
    