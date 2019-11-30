import Model.FilesReader;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Main {

    public static void main(String[] args)
    {
        System.out.println("Hello World!");

        long start = System.currentTimeMillis();

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Runnable test = new FilesReader("C:\\Users\\user1\\Desktop\\masters\\השלמה\\information_retrieval\\corpus\\corpus_test");
        
        //((FilesReader) test).GetListOfDirs();
        
        for(int i=0; i<1 ; i++)
        {
            executorService.execute(test);
        }
        
        executorService.shutdown();

        while (!executorService.isTerminated()){}
        
        long elapsedTime = System.currentTimeMillis() - start;
        System.out.println("The time of program: " + elapsedTime);
    }
}
