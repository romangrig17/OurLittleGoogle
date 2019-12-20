import View.GUI;
import ViewModel.Manager;

import java.awt.image.ImageProducer;
import java.util.*;

public class Main {

    public static void main(String[] args)
    {
        System.out.println("Hello World!");

        long start = System.currentTimeMillis();
        GUI gui = new GUI();

       // boolean stemming = true;
       // Manager manager = new Manager("D:\\My Little Project\\corpus\\corpus","D:\\My Little Project\\PostingFile",stemming);


        long elapsedTime = System.currentTimeMillis() - start;
        double elapsedTimeD = (double) elapsedTime;
        System.out.println("The time of program: " + (elapsedTimeD/60000) + " Min");
    }
    
    
    

}
    
    