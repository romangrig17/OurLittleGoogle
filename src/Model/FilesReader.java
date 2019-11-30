package Model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.io.File;


public class FilesReader implements IFilesReader,Runnable{

    private String rootPath;
    private String[] directories;

    public FilesReader(String path)
    {
    	rootPath = path;
    	SetListOfDirs(path);
    }
    
    @Override
    public void run() {
        ReadAll();
    }

    /**
     * Reads all the files, then separate them to Doc`s and send the Doc`s to Parse
     * when they back the function send them to write function
     */
    @Override
    public void ReadAll() {
    	for (int dirCounter=0; dirCounter<GetNumOfDirs(); dirCounter++)
        {
            File currentDirectory = new File(directories[dirCounter]);
            
            if (currentDirectory.isDirectory() == false)
            {
            	continue;
            }
            
            ReadFromDirectory(currentDirectory);
            
            dirCounter++;
        }
    }
    
    private void ReadFromDirectory(File directory) {
        String dirName = directory.getPath();
        String[] docsInDir = directory.list();
        
        for (int i=0; i<docsInDir.length ; i++)
        {
        	File file = new File(dirName + "\\" + docsInDir[i]);
        	ReadFile(file);
        }
    }

	private void ReadFile(File file) {
		
		 Parser parser = new Parser();
		try {
		    BufferedReader br = new BufferedReader(new FileReader(file));
		    String st;
		    int counterOfDocIncurrentFile = 1;
		    // make a String with only String
      
		    while ((st = br.readLine()) != null) {
		        if (st.contains("<TEXT>")) {
		            StringBuilder onlyText = new StringBuilder();
		            
		            while (!((st = br.readLine()).contains("</TEXT>"))) {
		                onlyText.append(st);
		            }

		          
		            parser.Parser(onlyText.toString(), file.getPath(), counterOfDocIncurrentFile++);
		            //WriteToFile(onlyText, rememberPath + "_" + counterOfDocInOneText++);
		        }
		    }
		    br.close();
		}
		catch (Exception e) {
		    System.out.println("problem with the reading from file!! in: " + file.getPath());
		}
	}

    @Override
    public void WriteToFile(StringBuilder document,String path) {
        //mutex.lock();
        try (PrintWriter out = new PrintWriter(path + ".txt")) {
            out.println(document);
            //mutex.unlock();
        }
        catch (Exception e)
        {
            //mutex.unlock();
            System.out.println("Problem with the writing");
        }
    }

    /**
     * Booting all parameters.
     * Fills the list of all Text files
     * @param path -  to folder where all docs is existing
     */
	private void SetListOfDirs(String path) {
        File files = new File(path);
        String[] names = files.list();
        directories = new String[names.length];

        for(int i=0; i<names.length; i++)
        {
        	directories[i] = path + "\\" + names[i];
        }
    }

    private int GetNumOfDirs() {
        if (directories != null)
        {
            return directories.length;
        }
        else {
            System.out.println("There Is No List Of Dirs");
            return 0;
        }
    }
}
