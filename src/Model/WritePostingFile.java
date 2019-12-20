package Model;

import java.io.*;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.regex.Pattern;

public class WritePostingFile extends Thread {

    //path where we write the posting files
    StringBuilder pathToWrite;

    //an hash map for Entity`s - if the list size >2 we will write them.
    HashMap<String, LinkedList<String>> h_Entity;

    //the posting files we got
    HashSet<String> namesOfPostingFile;

    //hash map for each document gets semaphore

    //ArrayList<Semaphore> arrayList;

    //posting file for each new thread
    private HashMap<String, HashMap<String, Integer>> postingFile;

    //hash map to collect the terms by theirs hash code(name of posting file in disk)
    HashMap<Integer, HashSet<String>> termsByHashCode;

    //amount of the posting files we write
    private static final int AMOUNT_OF_POSTING_FILES = 1000;

    private static HashMap<String, Semaphore> semaphoreHashMap;
    static {
        semaphoreHashMap = new HashMap<>();
        for (int i=0 ; i<AMOUNT_OF_POSTING_FILES; i++)
        {
            semaphoreHashMap.put(Integer.toString(i), new Semaphore(1));
        }
    }

    /**
     * initializing all params
     *
     * @param pathToWrite - to where we want write the posting files
     */
    public WritePostingFile(StringBuilder pathToWrite) {
        this.pathToWrite = pathToWrite;
        this.h_Entity = new HashMap<>();
        this.namesOfPostingFile = new HashSet<>();
    }

    /**
     * This function gets whole posting file from manager of many docs and works on this posting file only
     *
     * @param postingFile - posting files we want to write on disk
     */
    public void putPostingFile(HashMap<String, HashMap<String, Integer>> postingFile) {
        this.postingFile = postingFile;
    }

    @Override
    public void run() {
        toWrite(postingFile);
    }


    public boolean toWrite(HashMap<String, HashMap<String, Integer>> postingFile) {
        //gets the terms that need to be written by them hashcode
        getPackages(postingFile, AMOUNT_OF_POSTING_FILES);

        for (Integer termHashCode : termsByHashCode.keySet()) {
            try{
                if (namesOfPostingFile.contains(termHashCode.toString())) {
                    //we will read all the text in the file - each line we will put to ArrayList
                    //0 - text , 1- path
                    StringBuilder[] infoPostingFile = getTextFromFile(pathToWrite, termHashCode.toString(), termsByHashCode.get(termHashCode));
                    if (infoPostingFile == null) {
                        System.out.println("Problem to read from text in: " + termHashCode);
                        continue;
                    }
                    StringBuilder updatedText = infoPostingFile[0];
                    updateThePostingFile(infoPostingFile[1], updatedText, termHashCode.toString());

                } else {
                    StringBuilder substance = new StringBuilder();
                    for (String term : termsByHashCode.get(termHashCode)) {
                        //if its not Entity and that is a new posting file

                        //add to hash set that we have a file like this
                        namesOfPostingFile.add(termHashCode.toString());
                        //adding the term to line
                        substance.append(term).append(":");
                        //adding all the posting to line
                        for (String docName : postingFile.get(term).keySet()) {
                            substance = substance.append(docName).append("#").append(postingFile.get(term).get(docName)).append(",");
                        }
                        substance.append("\n");
                    }
                    writeToDiskNewTextFile(pathToWrite, termHashCode.toString(), substance.toString());
                }
            }catch (Exception e)
            {
                e.toString();
            }
        }

        return false;
    }


    private StringBuilder[] getTextFromFile(StringBuilder pathToWrite, String nameOfPostingFile, HashSet<String> hashOfTerms) {
        try {
            semaphoreHashMap.get(nameOfPostingFile).acquire();
            StringBuilder fullPath = new StringBuilder(pathToWrite).append("\\").append(nameOfPostingFile).append(".txt");
            File file = new File(fullPath.toString());
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            StringBuilder text = new StringBuilder();
            while ((line = br.readLine()) != null) {
                //if the term is already in the posting file - we will update the line and put the new to
                String[] lineSplit = line.split(":");
                if ((hashOfTerms.contains(lineSplit[0].toUpperCase())) || (hashOfTerms.contains(lineSplit[0].toLowerCase()))) {
                    //if we got our term like in the line with upper letters
                   if ((hashOfTerms.contains(lineSplit[0].toUpperCase())))
                   {
                       String termInPostingFile = lineSplit[0].toUpperCase();
                       text.append(lineSplit[0]).append(":").append(lineSplit[1]);
                       Set<String> set_DocsName = postingFile.get(termInPostingFile).keySet();
                       for (String docName : set_DocsName) {
                           text.append(docName).append("#").append(postingFile.get(termInPostingFile).get(docName)).append(",");
                       }
                       text.append("\n");
                       hashOfTerms.remove(lineSplit[0].toUpperCase());
                   }
                   //if we got our term like in the line with lower letters
                   else
                   {
                       String termInPostingFile = lineSplit[0].toLowerCase();
                       text.append(termInPostingFile).append(":").append(lineSplit[1]);
                       Set<String> set_DocsName = postingFile.get(termInPostingFile).keySet();
                       for (String docName : set_DocsName) {
                           text.append(docName).append("#").append(postingFile.get(termInPostingFile).get(docName)).append(",");
                       }
                       text.append("\n");
                       hashOfTerms.remove(lineSplit[0].toLowerCase());
                   }

                } else {
                    text.append(line).append("\n");
                }
            }
            if (hashOfTerms.size() > 0) {
                for (String term : hashOfTerms) {
                    text.append(term).append(":");
                    Set<String> set_DocsName = postingFile.get(term).keySet();
                    for (String docName : set_DocsName) {
                        text.append(docName).append("#").append(postingFile.get(term).get(docName)).append(",");
                    }
                    text.append("\n");
                }
                //not founded we will add him to end
            }
            br.close();
            StringBuilder[] infoPostingFile = new StringBuilder[2];
            infoPostingFile[0] = text;
            infoPostingFile[1] = fullPath;
            semaphoreHashMap.get(nameOfPostingFile).release();
            return infoPostingFile;
        } catch (Exception e) {
            /**
             * TODO: delete here tempForDebugger
             */
            StringBuilder tempForDebugger1 = pathToWrite;
            String tempForDebugger2 = nameOfPostingFile;
            e.toString();
            return null;
        }
    }

    private void updateThePostingFile(StringBuilder path, StringBuilder textInFile, String termHashCode) {
        /**
         * TODO:check what faster, update or delete and write new
         */
        try {
            semaphoreHashMap.get(termHashCode).acquire();
            File file = new File(path.toString());
            FileWriter writer = new FileWriter(file);
            writer.write(textInFile.toString());
            writer.close();
            semaphoreHashMap.get(termHashCode).release();
        } catch (Exception e) {
            System.out.println("Problem To Update The File: path12: " + path + ", The Text: " + textInFile);
        }
    }

    private void writeToDiskNewTextFile(StringBuilder path, String textName, String textInFile) {
        try {
            semaphoreHashMap.get(textName).acquire();
            StringBuilder pathToWrite = new StringBuilder(path).append("\\").append(textName).append(".txt");
            File file = new File(pathToWrite.toString());
            //Create the file
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.write(textInFile);
            writer.close();

            semaphoreHashMap.get(textName).release();
        } catch (Exception e) {
            System.out.println("Problem To Write The File: File Name13: " + textName + ", The Text: " + textInFile);
        }
    }


    //<editor-fold des="Entity">

    /**
     * TODO: check this function when i get Entity
     *
     * @param postingFile - posting file that we have
     * @param term        - the NTT
     */
    synchronized private void addNewEntity(HashMap<String, HashMap<String, Integer>> postingFile, String term) {
        Set<String> set_DocsName = postingFile.get(term).keySet();
        //if we got the entity
        if (h_Entity.containsKey(term)) {
            for (String docName : set_DocsName) {
                h_Entity.get(term).add(docName + "#" + postingFile.get(term).get(docName) + ",");
            }
        }
        //if we see Entity for the first time
        else {
            LinkedList<String> listOfDocs = new LinkedList<>();
            for (String docName : set_DocsName) {
                listOfDocs.add(docName + "#" + postingFile.get(term).get(docName) + ",");
                h_Entity.put(term, listOfDocs);
            }
        }
    }


    public void writeTheEntity(HashMap<String,String> dictionary) {
        if (h_Entity.size() < 1) {
            return;
        }
        StringBuilder entityFile = getStringForEntityFile(dictionary);
        try {
            String pathToWriteTheEntity = pathToWrite + "\\Entity.txt";
            File file = new File(pathToWriteTheEntity);
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.write(entityFile.toString());
            writer.close();
        } catch (Exception e) {
            System.out.println("Problem to write the Entity file");
            e.toString();
        }
    }


    private StringBuilder getStringForEntityFile(HashMap<String,String> dictionary) {
        StringBuilder entityFile = new StringBuilder();
        //delete each entity that appears only one time
        for (String entity : h_Entity.keySet()) {
            if (h_Entity.get(entity).size() > 1) {
                entityFile.append(entity.substring(1)).append(":");
                for (String infoOnEntity : h_Entity.get(entity)) {
                    entityFile.append(infoOnEntity);
                }
                entityFile.append("\n");
            }
            else
            {
                dictionary.remove(entity);
            }
        }
        return entityFile;
    }
    //</editor-fold>

    /**
     * Sort all terms by theirs hash code
     * We want to write together all the terms that have the same hash code - (same posting file name on disk)
     * @param postingFile          - posting file from many docs
     * @param amountOfPostingFiles - amount on posting files we want on our disk
     */
    synchronized private void getPackages(HashMap<String, HashMap<String, Integer>> postingFile, int amountOfPostingFiles) {
        termsByHashCode = new HashMap<>();
        for (String term : postingFile.keySet()) {
            if (term.charAt(0) == '!') {
                addNewEntity(postingFile, term);
                //postingFile.remove(term);
                continue;
            }
            Integer termHashCode = (Math.abs(term.toLowerCase().hashCode() % amountOfPostingFiles));
            if (termsByHashCode.containsKey(termHashCode)) {
                termsByHashCode.get(termHashCode).add(term);
            } else {
                HashSet<String> temp = new HashSet<>();
                temp.add(term);
                termsByHashCode.put(termHashCode, temp);
            }
        }
    }

    public void changePath(StringBuilder newPath)
    {
        this.pathToWrite = newPath;
    }
}
