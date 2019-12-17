package Model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;

public class StopWords {

    private HashSet<String> allStopWords;

    public StopWords(String path)
    {
        allStopWords = new HashSet<>();
        File file = new File(path);
        try {
            String st = "";
            BufferedReader br = new BufferedReader(new FileReader(file));
            while ((st = br.readLine()) != null) {
                //gets the name of the text
                allStopWords.add(st);
            }
            br.close();
        }
        catch (Exception e) {
            System.out.println("problem with the reading from file!! in: " + file.getPath());
        }
    }

    public void removeStopWords(HashMap<String,Integer> listOfTerms)
    {
        for (String stopWord : allStopWords)
        {
            listOfTerms.remove(stopWord);
        }
    }

}
