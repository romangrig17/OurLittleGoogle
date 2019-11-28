package Model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.io.File;


public class ReadTheFile implements ReadFile,Runnable{

    private ArrayList<String> allTextFile;
    private String path;
    private int counter = 0;
    private String[] names;
    //private Mutex mutex;
    //protected Semaphore;

    @Override
    public void run() {
        startToRead();
    }

    /**
     * Reads all the files, then separate them to Doc`s and send the Doc`s to Parse
     * when they back the function send them to write function
     */
    @Override
    public void startToRead() {


        int numOfDirs  = getNumOfDirs();
        while (numOfDirs != counter){
            //every thread gets his file
            //mutex.lock();
            String rememberPath = allTextFile.get(counter);
            File file = new File(rememberPath);
            counter++;
            //mutex.unlock();

            //need to check if there is a problem with thread
            //I checked and everything looked fine!
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String st;
                int counterOfDocInOneText = 1;
                // make a String with only String
                while ((st = br.readLine()) != null) {
                    if (st.contains("<TEXT>")) {
                        String onlyText = "";
                        while (!((st = br.readLine()).contains("</TEXT>"))) {
                            onlyText = onlyText + st;
                        }

                        Parser parser = new Parser();
                        parser.Parser(onlyText,names[counter],counterOfDocInOneText);
                        //writheFile(onlyText, rememberPath + "_" + counterOfDocInOneText++);
                    }
                }
            }
            catch (Exception e) {
                System.out.println("problem with the reading from file!! in: " + rememberPath);
            }

        }
    }

    @Override
    public void writheFile(StringBuilder document,String path) {
        //mutex.lock();
        try (PrintWriter out = new PrintWriter(path + ".txt")) {
            out.println(document);
            //mutex.unlock();
        }
        catch (Exception e)
        {
            //mutex.unlock();
            System.out.println("Problem With the writhing");
        }
    }

    /**
     * Booting all parameters.
     * Fills the list of all Text files
     * @param path -  to folder where all docs is existing
     */
    @Override
    public void getListOfDirs(String path) {
        //Booting all parameters

        //mutex = new Mutex();
        this.path = path;
        this.allTextFile = new ArrayList<>();
        //brings all the paths of dirs that exist in our path
        File files = new File(path);
        files.listFiles();
        names = files.list();


        for(String name : names)
        {
            String pathWithName = path +"\\" + name;
            if (new File(pathWithName).isDirectory())
            {
                File docs = new File(pathWithName);
                String[] allDocsInDir = docs.list();
                for (int i=0; i<allDocsInDir.length ; i++)
                {
                    allTextFile.add(pathWithName + "\\" + allDocsInDir[i]);
                }
            }
        }
    }

    private int getNumOfDirs() {
        if (allTextFile != null)
        {
            return allTextFile.size();
        }
        else {
            System.out.println("There Is No List Of Dirs");
            return 0;
        }
    }

}
