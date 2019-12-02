package Model;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.regex.Pattern;


enum types
{
	UNDEFINED,
	NUMBER_SMALLER_THAN_1K, // 0<=x<1K
	NUMBER_1K_TO_1M, // 1K<=x<1M
	NUMBER_1M_TO_1B, // 1M<=x<1B
	NUMBER_GREATER_THAN_1B, //x>=1B
	NUMBER_ENDS_WITH_$,
	NUMBER_ENDS_WITH_SIGN_PERCENTAGE,// 123%
	NUMBER_ENDS_WITH_SIGN_$, //23$
	NUMBER_FRACTION // 1/2
}

public class Parser implements IParser{

    public ArrayList<Pair<String,String>> Parser(String doc_Text, String doc_Name, int doc_Number)
    {
        //first string is the name, second string is the name of doc.
        ArrayList<Pair<String,String>> listOfAllTerms = new ArrayList();
        /**
         * TODO: to check the sentence between " " (our first rule).
         */
        //first we need to split and find the text between ""
        String[] allWords = doc_Text.split(" ");
        int allWordsLength = allWords.length;
        String theWordBefore="";
        String word;
        boolean end_with_dot=false;  
        
        for(int i = 0; i<allWordsLength; i++)
        {
            word = allWords[i].trim();
            if (word.isEmpty())
            	continue;
            
            if(end_with_dot= word.endsWith("."))
            {
            	word = word.substring(0,word.length()-1);
            }
            
            if(Pattern.matches("(\\d{1,3},\\d{3}(,\\d{3})*)(\\.\\d*)?|\\d+\\.?\\d*([$%])?",word))//number - might end with %/$
            {
            	if(word.endsWith("$"))
            	{
                	System.out.println("ends with dollar: " +word);	
            	}
            	else if(word.endsWith("%"))
            	{
                	System.out.println("ends with %: " +word);	
            	}
                else //number
                {
                	if( (!end_with_dot) && ((i+1)<allWordsLength) )
                	{//look if word after is "percentage" or "percent" or "%" / "Dollars" or "$" /  
                		//if allWords[i+1]=="percentage"/"percent"/"%", save as number%
                		//if allWords[i+1]=="Dollars" or "$"or "million/billion/trillion/m/bn/"
                		//if allWords[i+1]==// fraction with / - 3/4
                		//else- goto else underneath 
                		System.out.println("check next: " +word );
                	}

            		//TODO- insert number as term- before check the size, 
            		//to see if we need to save the number with K/M/B
                	word=word.replaceAll(",","");
            		types e= checkNumSize(word);
            		System.out.println("num type is : " +e.toString() );
                }
            	System.out.println("matched in number: " +word );
            }
            else if(Pattern.matches("(\\d{1,}+\\/+\\d{1,})",word))// fraction with / - 3/4 , 12/34
            {
            	System.out.println("fraction with / : " +word);		
            }
            else if(Pattern.matches("^[+]*[(]{0,1}[0-9]{1,4}[)]{0,1}[-\\s\\./0-9]*$",word))//phone number- TODO- check term i+1 for cases like (703) 44324 -  
            {
            	System.out.println("Phone number: " +word);
            }
            else if(Pattern.matches("[a-zA-Z]*",word))//words
            {
            	System.out.println("words: " +word);	
            }
            
            theWordBefore = word;
        }
        //docToParse
        return listOfAllTerms;
    }
    

    private types checkNumSize(String number)
    {
    	types ret=types.UNDEFINED;
    	long num; 
    	
    	 try
    	 {
    		 num=Long.parseLong(number);
     		if(num<1000) 
     		{
     	    	ret=types.NUMBER_SMALLER_THAN_1K;
     		}
     		else if(num<1000000)
     		{
     	    	ret=types.NUMBER_1K_TO_1M;	
     		}
     		else if(num<1000000000)
     		{
     			ret=types.NUMBER_1M_TO_1B;		
     		}
     		else
     		{
     			ret=types.NUMBER_GREATER_THAN_1B;	
     		}
    	 }
    	 catch (NumberFormatException e)
    	 {
    		 System.out.println("ERROR:checkNumSize - " + number);
    	 }
    	
    	return ret;
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
