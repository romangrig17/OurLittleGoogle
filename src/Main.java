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
        Manager manager = new Manager("C:\\Users\\user1\\Desktop\\masters\\השלמה\\information_retrieval\\corpus\\corpus_test","C:\\Users\\user1\\Desktop\\masters\\השלמה\\information_retrieval\\corpus\\corpus_write",stemming);


        //<editor-fold des="old start">
        //          ExecutorService executorService = Executors.newFixedThreadPool(1);
//                  Runnable test = new ReadFile("C:\\Users\\roman\\OneDrive\\שולחן העבודה\\My Little Project\\corpus\\corpus");
//
//                  //((FilesReader) test).GetListOfDirs();
//
//                  for(int i=0; i<1 ; i++)
//                  {
//                      executorService.execute(test);
//                  }
//
//                  executorService.shutdown();
//
//                  while (!executorService.isTerminated()){}
//

        //</editor-fold>


        long elapsedTime = System.currentTimeMillis() - start;
        System.out.println("The time of program: " + elapsedTime);
    }
    
    
    

}
    
    