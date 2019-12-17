package Model;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class WritePostingFile extends Thread  {

    /**
     * TODO: change all to string builder, read with buff reader
     */
    StringBuilder pathToWrite;
    //an hash map for NTT`s - if the list size >2 we will write them.
    HashMap<String, LinkedList<String>> h_NTT;
    HashSet<String> namesOfPostingFile;


    private HashMap<String, HashMap<String,Integer>> postingFile;
    HashMap<Integer,HashSet<String>> termsByHashCode;
    //HashMap<String, HashMap<String,Integer>> postingFile

    /**
     * init all params
     * @param pathToWrite - to where we want write
     */
    public WritePostingFile(StringBuilder pathToWrite) {
        this.pathToWrite = pathToWrite;
        this.h_NTT = new HashMap<>();
        this.namesOfPostingFile = new HashSet<>();
        //this.postingFile = postingFile;
    }

    public void putPostingFile(HashMap<String, HashMap<String, Integer>> postingFile)
    {
        this.postingFile = postingFile;
    }

    @Override
    public void run() {
        toWrite(postingFile);
    }

    synchronized public boolean toWrite(HashMap<String, HashMap<String,Integer>> postingFile)
    {
        //goes on all terms
        /**
         * TODO: check each term if he got the same at Dic in low letters
         */
        getPackages(postingFile,250);

        for (Integer termHashCode : termsByHashCode.keySet())
        {
            if (namesOfPostingFile.contains(termHashCode.toString()))
            {
                //we will read all the text in the file - each line we will put to ArrayList
                //0 - text , 1- path
                StringBuilder[] infoPostingFile = getTextFromFile(pathToWrite,termHashCode.toString(),termsByHashCode.get(termHashCode));
                if (infoPostingFile == null)
                    System.out.println("Problem to read from text in: " + termHashCode);
                StringBuilder updatedText = infoPostingFile[0];
                updateThePostingFile(infoPostingFile[1],updatedText);

            }
            else
            {//here!!
                // if the term is NTT
                StringBuilder substance = new StringBuilder();
                for (String term : termsByHashCode.get(termHashCode))
                {
                    if (Pattern.matches("[_ .,-;'/\t]",term))
                    {
                        addNewNtt(postingFile,term);
                    }
                    //if its not NTT and that is a new posting file
                    else
                    {
                        //add to hash set that we have a file like this
                        namesOfPostingFile.add(termHashCode.toString());
                        //adding the term to line
                        substance.append(term).append(":");
                        //adding all the posting to line
                        for (String docName : postingFile.get(term).keySet())
                        {
                            substance = substance.append(docName).append("#").append(postingFile.get(term).get(docName)).append(",");
                        }
                        substance.append("\n");
                    }
                }
                writeToDiskNewTextFile(pathToWrite,termHashCode.toString(),substance.toString());
            }//else - if there is no text file with this name
        }

        return false;
    }


    synchronized private StringBuilder[] getTextFromFile(StringBuilder pathToWrite, String nameOfPostingFile,HashSet<String> hashOfTerms)
    {
        try
        {
            StringBuilder fullPath =(new StringBuilder(pathToWrite).append("\\").append(nameOfPostingFile).append(".txt"));
            File file = new File(fullPath.toString());
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            StringBuilder text = new StringBuilder();
            while ((line = br.readLine()) != null) {
                //if the term is already in the posting file - we will update the line and put the new to
                String[] lineSplit = line.split(":");
                if((hashOfTerms.contains(lineSplit[0].toUpperCase())) || (hashOfTerms.contains(lineSplit[0].toLowerCase())))
                {
                    String originalTerm = lineSplit[0];
                    String termInPostingFileOfRam = lineSplit[0];
                    if (hashOfTerms.contains(lineSplit[0].toLowerCase()))
                    {
                        termInPostingFileOfRam = lineSplit[0].toLowerCase();
                        originalTerm = termInPostingFileOfRam;
                    }
                    //TODO: check if new term is with low or upper case and this what to do - its cannot be the same doc!!
                    //need to change the term and update the line
                    String originalTermInThePostingFileDisk = lineSplit[0];
                    StringBuilder infoOnTerm = new StringBuilder(lineSplit[1]);
                    //update the all line
                    Set<String> set_DocsName = postingFile.get(termInPostingFileOfRam).keySet();
                    for(String docName : set_DocsName)
                    {
                        infoOnTerm.append(docName).append("#").append(postingFile.get(termInPostingFileOfRam).get(docName)).append(",");
                    }
                    text.append(originalTerm).append(":").append(infoOnTerm).append("\n");
                    hashOfTerms.remove(termInPostingFileOfRam);
                }
                else
                {
                    text.append(line).append("\n");
                }
            }
            if (hashOfTerms.size()>0)
            {
                for (String term : hashOfTerms)
                {
                    text.append(term).append(":");
                    Set<String> set_DocsName = postingFile.get(term).keySet();
                    for(String docName : set_DocsName)
                    {
                        text.append(docName).append("#").append(postingFile.get(term).get(docName)).append(",");
                    }
                    text.append("\n");
                }
                //not founded we will add him to end
            }
            StringBuilder[] infoPostingFile = new StringBuilder[2];
            infoPostingFile[0] = text;
            infoPostingFile[1] = fullPath;
            return infoPostingFile;
        }catch (Exception e)
        {
            e.toString();
        }
        return null;
    }

    synchronized private void updateThePostingFile(StringBuilder path, StringBuilder textInFile)
    {
        File file = new File(path.toString());
        /**
         * TODO:check what faster, update or delete and write new
         */
        //file.delete();
        //BufferedWriter out = new BufferedWriter(new FileWriter(file), 32768);
        try {
            //BufferedWriter out = new BufferedWriter(new FileWriter(file), 32768);
            //out.write(textInFile.toString());
            //out.close();
            //file.createNewFile();
            //we can delete what exist and write new

            FileWriter writer = new FileWriter(file);
            writer.write(textInFile.toString());
            writer.close();
        }
        catch (Exception e)
        {
            System.out.println("Problem To Update The File: path: " + path +", The Text: " + textInFile);
        }
    }

    synchronized private void writeToDiskNewTextFile(StringBuilder path, String textName, String textInFile)
    {
        StringBuilder pathToWrite = new StringBuilder(path).append("\\").append(textName).append(".txt");
        File file = new File(pathToWrite.toString());
        try {
            //Create the file
            file.createNewFile();
//            BufferedWriter out = new BufferedWriter(new FileWriter(file), 32768);
//            out.write(textInFile.toString());
//            out.close();

            FileWriter writer = new FileWriter(file);
            writer.write(textInFile);
            writer.close();
        }
        catch (Exception e)
        {
            System.out.println("Problem To Write The File: File Name: " + textName +", The Text: " + textInFile);
        }
    }

    /**
     * TODO: check this function when i get NTT
     * @param postingFile - posting file that we have
     * @param term - the NTT
     */
    synchronized private void addNewNtt(HashMap<String, HashMap<String,Integer>> postingFile,String term)
    {
        Set<String> set_DocsName = postingFile.get(term).keySet();
        if (h_NTT.containsKey(term))
        {
            LinkedList<String> listOfDocs = new LinkedList<>();
            for(String docName : set_DocsName)
            {
                listOfDocs.add(docName + "#" + postingFile.get(term).get(docName));
            }
            h_NTT.put(term,listOfDocs);
        }
        //if we see NTT for the first time
        else
        {
            LinkedList<String> temp = new LinkedList<>();
            for(String docName : set_DocsName)
            {
                temp.add(docName + "#" + postingFile.get(term).get(docName) + ",");
                h_NTT.put(term,temp);
            }
        }
    }
    
    private void getPackages(HashMap<String, HashMap<String,Integer>> postingFile, int amountOfPostingFiles)
    {
        termsByHashCode = new HashMap<>();
        for (String term : postingFile.keySet())
        {
            Integer termHashCode = (term.toLowerCase().hashCode()%amountOfPostingFiles);
            if (termsByHashCode.containsKey(termHashCode))
            {
                termsByHashCode.get(termHashCode).add(term);
            }
            else
            {
                HashSet<String> temp = new HashSet<>();
                temp.add(term);
                termsByHashCode.put(termHashCode,temp);
            }
        }
    }
}
