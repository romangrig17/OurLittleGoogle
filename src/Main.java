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

//        String st1 = "the";
//        String st2 = "THE";
//        Integer in1 = (Math.abs(st1.toLowerCase().hashCode() % 2500));
//        Integer inw = (Math.abs(st2.toLowerCase().hashCode() % 2500));

        boolean stemming = true;
        Manager manager = new Manager("D:\\My Little Project\\corpus\\corpus","D:\\My Little Project\\PostingFile",stemming);


        long elapsedTime = System.currentTimeMillis() - start;
        double elapsedTimeD = (double) elapsedTime;
        System.out.println("The time of program: " + (elapsedTimeD/60000) + " Min");
    }
    
    
    

}
    
    