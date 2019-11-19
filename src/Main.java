import Model.ReadTheFile;import sun.nio.ch.ThreadPool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Main {

    public static void main(String[] args)
    {
        System.out.println("Hello World!");

        long start = System.nanoTime();

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        Runnable test = new ReadTheFile();
        ((ReadTheFile) test).getListOfDirs("C:\\Users\\roman\\OneDrive\\שולחן העבודה\\My Little Project\\corpus\\corpus");
        for(int i =0; i<10 ; i++)
        {
            executorService.execute(test);
        }
        executorService.shutdown();

        while (!executorService.isTerminated()){}
        long elapsedTime = System.nanoTime() - start;
        System.out.println("The time of program: " + elapsedTime);


    }
}
