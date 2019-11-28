package Model;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.regex.Pattern;


public class Parser implements Parse{

    public ArrayList<Pair<String,String>> Parser(String doc_Text, String doc_Name,int doc_Number)
    {
        //first string is the name, second string is the name of doc.
        ArrayList<Pair<String,String>> listOfAllTerms = new ArrayList();
        /**
         * TODO: to check the sentence between " " (our first rule).
         */
        //first we need to split and find the text between ""
        String[] allWords = doc_Text.split(" ");
        int allWordsLength = allWords.length;
        for(int i = 0; i<allWordsLength; i++)
        {
            String word = allWords[i];
            //if the word is empty
            if(word.isEmpty())
            {
                continue;
            }
            /**
             * if the word is a number
             * can catch number with a dot at the end
             * TODO: need to check what is the word before and what the word after for dollars and other values
             */
            else if(Pattern.matches("^[0-9]+\\.?[0-9]*",word))
            {
                numberParsser(word);
                //System.out.println(word);
            }
            String theWordBefore = word;
        }
        //docToParse
        return listOfAllTerms;
    }

    private String numberParsser(String term)
    {
        String termToReturn = "";
        //remove the dot from the end of the number
        if (term.endsWith("."))
        {
            termToReturn = term.substring(0,term.length()-1);
            //System.out.println(termToReturn);

        }
        //if the word is a number and he got more then 3 numbers after the dot (.)
        if(Pattern.matches("[-+]?[0-9]+\\.[0-9]{4,}",term))
        {

        }
        return termToReturn;
    }
}
