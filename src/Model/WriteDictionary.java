package Model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;

public class WriteDictionary {

    String pathToWrite;

    //Constructor
    public WriteDictionary() {
    }


    /**
     * this function write the dictionary to disk
     *
     * @param dictionary - gets the dictionary we got
     */
    public void run(HashMap<String, String> dictionary) {
        StringBuilder dictionaryToWrite = new StringBuilder();
        for (String term : dictionary.keySet()) {
            if(term.charAt(0) == '!')
            {
                dictionaryToWrite.append(term.substring(1)).append(":").append(dictionary.get(term)).append("\n");
            }
            else
            {
                dictionaryToWrite.append(term).append(":").append(dictionary.get(term)).append("\n");
            }
        }
        try {
            File file = new File((pathToWrite + "\\Dictionary.txt"));
            FileWriter writer = new FileWriter(file);
            writer.write(dictionaryToWrite.toString());
            writer.close();
        } catch (Exception e) {
            System.out.println("Problem To Update The File: path12: " + pathToWrite + ", The dictionary");
        }
    }

    /**
     * this function read the dictionary from the disk
     *
     * @return - dictionary
     */
    public HashMap<String, String> loadDictionary() {
        HashMap<String, String> dictionary = new HashMap<>();
        File file = new File((this.pathToWrite + "\\Dictionary.txt"));
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitLine = line.split(":");
                dictionary.put(splitLine[0], splitLine[1]);
            }
        } catch (Exception e) {
            e.toString();
        }
        return dictionary;
    }

    /**
     * Setters
     *
     * @param path         - path to write / read from
     * @param stemming     - what dictionary we want stemming
     * @param needToChange - if we need to change the path
     */
    public void setPathToWrite(String path, boolean stemming, boolean needToChange) {
        if (stemming && needToChange) {
            this.pathToWrite = path + "\\With Stemming";
        } else if (needToChange) {
            this.pathToWrite = path + "\\Without Stemming";
        } else {
            this.pathToWrite = path;
        }

    }
}
