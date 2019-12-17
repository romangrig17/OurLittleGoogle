package Model;

import java.util.HashMap;
import java.util.HashSet;

public class StopWords {

    private HashSet<String> allStopWords;

    public StopWords(String path, HashMap<String,Integer> listOfTerms)
    {
        addStopWordsToSet(path);
        for (String stopWord : allStopWords)
        {
            listOfTerms.remove(stopWord);
        }
    }

    private void addStopWordsToSet(String path)
    {

    }

}
