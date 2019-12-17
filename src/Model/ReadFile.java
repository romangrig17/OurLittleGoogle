package Model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.io.File;
import java.util.HashMap;


public class ReadFile implements IReadFile {

    private String rootPath;
    public String[] directories;
    ArrayList<String> allFiles;



    public ReadFile(String path)
    {
    	rootPath = path;
    	getListOfDirs(path);
        allFiles = new ArrayList<>();
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
        }
        System.out.println("im here");
    }

    /**
     * Getting all files from a dir
     * @param directory - the dir where is the files
     */
    private void ReadFromDirectory(File directory) {
        String dirName = directory.getPath();
        String[] docsInDir = directory.list();
        
        for (int i=0; i<docsInDir.length ; i++)
        {
        	File file = new File(dirName + "\\" + docsInDir[i]);
        	allFiles.add(dirName + "\\" + docsInDir[i]);
        }
    }

    /**
     * get the whole file and return the Texts from the file with DOC ID
     * @param file - getting the whole file/
     */
	public HashMap<String,StringBuilder> getTextsFromTheFile(File file) {
		HashMap<String,StringBuilder> allDocsInTheFile = new HashMap<>();
		// Parser parser = new Parser();
		try {
		    BufferedReader br = new BufferedReader(new FileReader(file));
		    String st;
            String docName = "";
		    while ((st = br.readLine()) != null) {
                //gets the name of the text

                if (st.contains("<DOCNO>"))
                {
                    String[] line = st.split(" ");
                    docName = line[1];
                }
		        else if (st.contains("<TEXT>")) {
		            StringBuilder onlyText = new StringBuilder();
		            
		            while (!((st = br.readLine()).contains("</TEXT>"))) {
		                onlyText.append(st);
		            }
		            allDocsInTheFile.put(docName,onlyText);
		        }
		    }
		    br.close();
		}
		catch (Exception e) {
		    System.out.println("problem with the reading from file!! in: " + file.getPath());
		}
		return allDocsInTheFile;
	}




    //<editor-fold des="Getters">
    /**
     * Fills the list of all Text files
     * @param path -  to folder where all docs is existing
     */
    private String[] getListOfDirs(String path) {
        File files = new File(path);
        String[] names = files.list();
        directories = new String[names.length];

        for(int i=0; i<names.length; i++)
        {
            directories[i] = path + "\\" + names[i];
        }
        return directories;
    }

    /**
     *
     * @return - number of dirs in the path
     */
    public int GetNumOfDirs() {
        if (directories != null)
        {
            return directories.length;
        }
        else {
            System.out.println("There Is No List Of Dirs");
            return 0;
        }
    }

    /**
     *
     * @return - all the files that we have in the corpus.
     */
    public ArrayList<String> getAllFiles()
    {
        return allFiles;
    }
    //</editor-fold>




}
