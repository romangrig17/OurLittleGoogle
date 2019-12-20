package Model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;

public class WriteDictionary {

    String pathToWrite;

    public WriteDictionary()
    {

    }

    public void run(HashMap<String, String> dictionary)
    {
        StringBuilder dictionaryToWrite = new StringBuilder();
        for (String term : dictionary.keySet())
        {
            dictionaryToWrite.append(term).append(":").append(dictionary.get(term)).append("\n");
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

    public HashMap<String, String> loadDictionary()
    {
        HashMap<String,String> dictionary = new HashMap<>();
        File file = new File((this.pathToWrite + "\\Dictionary.txt"));
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitLine = line.split(":");
                dictionary.put(splitLine[0],splitLine[1]);
            }
        }catch (Exception e){
            e.toString();
        }
        return dictionary;
    }

    public void setPathToWrite(String path,boolean stemming,boolean needToChange)
    {
        if (stemming && needToChange)
        {
            this.pathToWrite = path + "\\With Stemming";
        }
        else if (needToChange)
        {
            this.pathToWrite = path + "\\Without Stemming";
        }
        else
        {
            this.pathToWrite = path;
        }

    }
}
