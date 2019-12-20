package Model;

import java.util.*;

public class Indexer {

    //<editor-fold des="fields">
    /**
     * first string - term.
     * second string - "how many times appeared in the Corpus, in how many docs it was,the last doc where its appeared"
     * [term ; _ , _ , _ ]
     */
    HashMap<String, String> dictionary;

    /**
     * first string - term
     * second string - "DOCID # how many times in that doc"
     * [term ; DOCID # _ , DOCID # _ ...]
     */
    private HashMap<String, HashMap<String,Integer>> postingFile;
    //</editor-fold>


    public Indexer() {
        this.dictionary = new HashMap<>();
        this.postingFile = new HashMap<>();
    }

    public HashMap<String, HashMap<String,Integer>> getPostingFileFromListOfTerms(HashMap<String, Integer> listOfTerms, String docName)
    {
        for (String term: listOfTerms.keySet())
        {
            //check what we got from parser
            if (term.length() == 0|| term.charAt(0) == '['
                    || term.charAt(0) == ']' || term.charAt(0) == ')' || term.charAt(0) == ',' || term.charAt(0) == '"' ||
                    term.charAt(0) == '\'' || term.charAt(0) == '`' || term.charAt(0) == '_' || term.charAt(0) == ' ')
            {
                continue;
            }
            //make all the term with upper letters if the first letter is upper
            String originalTerm = term;
            boolean ifTheWordIsWithUpperLetters = false;
            if (originalTerm.charAt(0) >= 'A' && originalTerm.charAt(0) <= 'Z' && (originalTerm.charAt(0) != '!'))
            {
                originalTerm = originalTerm.toUpperCase();
                ifTheWordIsWithUpperLetters = true;
            }
            else if (originalTerm.charAt(0) >= 'a' && originalTerm.charAt(0) <= 'z' && (originalTerm.charAt(0) != '!'))
            {
                originalTerm = originalTerm.toLowerCase();
            }
            else if (originalTerm.charAt(0) >= '0' && originalTerm.charAt(0) <= '9' && originalTerm.contains("-"))
            {
                //number - word
                int indexOfChar = originalTerm.indexOf('-');
                if (originalTerm.charAt(indexOfChar +1) >= 'A' || originalTerm.charAt(indexOfChar +1) <= 'Z')
                {
                    originalTerm = originalTerm.toUpperCase();
                }
                else
                {
                    originalTerm = originalTerm.toLowerCase();
                }
            }

            if(dictionary.containsKey(originalTerm.toLowerCase()))
            {
                String[] splitedDic = dictionary.get(originalTerm.toLowerCase()).split(",");
                /**
                 * if the term is in dictionary with small letters
                 * we will add this word - (with the small letters) to dictionary and check if it is the same document
                 */
                if(ifTheWordIsWithUpperLetters)
                {
                    //the term is with upper letters and we got in the dictionary the same term with low letters
                    //need to update the dictionary - if its a same document or not but the same word
                    //update the posting file to - with the lower letters! only add this to posting
                    if (dictionary.get(originalTerm.toLowerCase()).split(",")[2].equals(docName))
                    {
                        //if its the same document
                        Integer updateNumberOfTimesInAllCorpus = Integer.parseInt(splitedDic[0]) + listOfTerms.get(term);
                        dictionary.put(originalTerm.toLowerCase(),updateNumberOfTimesInAllCorpus + "," +splitedDic[1] +"," +docName);
                        //if dictionary do not contains posting file for sure!
                        if(postingFile.containsKey(originalTerm.toLowerCase()))
                        {
                            updatePostingFileIfSameFile(originalTerm.toLowerCase(),docName,listOfTerms.get(term));
                        }
                        else
                        {
                            addToPostingFile(originalTerm.toLowerCase(),docName,listOfTerms.get(term));
                        }
                    }
                    else
                    {
                        Integer updateNumberOfTimesInAllCorpus = Integer.parseInt(splitedDic[0]) + listOfTerms.get(term);
                        Integer updateNumberOfDocuments = (Integer.parseInt(splitedDic[1])) + 1;
                        dictionary.put(originalTerm.toLowerCase(),updateNumberOfTimesInAllCorpus + "," + updateNumberOfDocuments + "," +docName);
                        if(postingFile.containsKey(originalTerm.toLowerCase()))
                        {
                            updatePostingFileIfNewFile(originalTerm.toLowerCase(),docName,listOfTerms.get(term));
                        }
                        else
                        {
                            addToPostingFile(originalTerm.toLowerCase(),docName,listOfTerms.get(term));
                        }
                    }
                }
                else
                //if the term we got was is low letters and we got him with low letters - first time in the doc
                {
                    Integer updateNumberOfTimesInAllCorpus = (Integer.parseInt(splitedDic[0])) + listOfTerms.get(term);
                    Integer updateNumberOfDocuments = (Integer.parseInt(splitedDic[1])) + 1;
                    dictionary.put(originalTerm.toUpperCase(),updateNumberOfTimesInAllCorpus + "," + updateNumberOfDocuments + "," +docName);
                    //we got the term with lower letters
                    //only need to update the dictionary and add to posting file!
                    if(postingFile.containsKey(originalTerm.toLowerCase()))
                    {
                        updatePostingFileIfNewFile(originalTerm.toLowerCase(),docName,listOfTerms.get(term));
                    }
                    else
                    {
                        addToPostingFile(originalTerm.toLowerCase(),docName,listOfTerms.get(term));
                    }
                }

            }
            else if(dictionary.containsKey(originalTerm.toUpperCase()))
            {
                /**
                 * if the word is in dictionary with big letters
                 * we will check first if the word is with small letters - and if it does we will change the dictionary and posting to small letters!
                 * we will add this word - (with the small letters) to dictionary and check if it is the same document
                 */
                String[] splitedDic = dictionary.get(originalTerm.toUpperCase()).split(",");
                if(ifTheWordIsWithUpperLetters)
                {
                    //if the word in dictionary was with upper letters and we got with upper letters too - new doc!
                    Integer updateNumberOfTimesInAllCorpus = Integer.parseInt(splitedDic[0]) + listOfTerms.get(term);
                    Integer updateNumberOfDocuments = (Integer.parseInt(splitedDic[1])) + 1;
                    dictionary.put(originalTerm,updateNumberOfTimesInAllCorpus + "," + updateNumberOfDocuments + "," +docName);
                    if (postingFile.containsKey(originalTerm))
                    {
                        updatePostingFileIfNewFile(originalTerm,docName,listOfTerms.get(term));
                    }
                    else
                    {
                        addToPostingFile(originalTerm,docName,listOfTerms.get(term));
                    }
                }
                else
                {
                    //we got term with small letters and we got big letters in dictionary
                    if (dictionary.get(originalTerm.toUpperCase()).split(",")[2].equals(docName))
                    {
                        int updateNumberOfTimesInAllCorpus = Integer.parseInt(splitedDic[0]) + listOfTerms.get(term);
                        dictionary.put(originalTerm,updateNumberOfTimesInAllCorpus + "," + splitedDic[1] + "," +docName);
                        if (postingFile.containsKey(originalTerm.toUpperCase()))
                        {
                            int amountBefore = postingFile.get(originalTerm.toUpperCase()).get(docName);
                            int thisAmount = listOfTerms.get(term);
                            HashMap infoDocs = postingFile.get(originalTerm.toUpperCase());
                            infoDocs.put(docName,amountBefore+thisAmount);
                            postingFile.put(originalTerm,infoDocs);
                        }
                        else
                        {
                            addToPostingFile(originalTerm,docName,listOfTerms.get(term));
                        }
                    }
                    else
                    {
                        int updateNumberOfTimesInAllCorpus = Integer.parseInt(splitedDic[0]) + listOfTerms.get(term);
                        int updateNumberOfDocuments = (Integer.parseInt(splitedDic[1])) + 1;
                        dictionary.put(originalTerm,updateNumberOfTimesInAllCorpus + "," + updateNumberOfDocuments + "," +docName);
                        if (postingFile.containsKey(originalTerm.toUpperCase()))
                        {
                            HashMap infoDocs = postingFile.get(originalTerm.toUpperCase());
                            infoDocs.put(docName,listOfTerms.get(term));
                            postingFile.put(originalTerm,infoDocs);
                        }
                        else
                        {
                            addToPostingFile(originalTerm,docName,listOfTerms.get(term));
                        }
                    }
                    //if the term we got now with lower letters and we got with upper
                    //removing the terms with upper letters from dictionary and posting file
                    postingFile.remove(originalTerm.toUpperCase());
                    dictionary.remove(originalTerm.toUpperCase());
                }
            }
            else
            {
                //if we see the term for the first time
                addToDictionary(originalTerm,listOfTerms.get(term),docName);
            }
        }// for
        return postingFile;
    }





    //<editor-fold des="adding to Dictionary>
    /**
     * adding new term to dictionary and after that to posting file
     * this function Refers to the situation that we found new term
     * @param term - the term we want to add to dictionary and posting file
     * @param howManyTimesAppearedInThisDoc - the number of times that word was in the text
     * @param docName - ID of document
     */
    private void addToDictionary(String term,Integer howManyTimesAppearedInThisDoc, String docName)
    {
        // if the term is for the first time in the dictionary
        dictionary.put(term, howManyTimesAppearedInThisDoc + ",1," + docName + ",");
        addToPostingFile(term,docName,howManyTimesAppearedInThisDoc);
    }

    //</editor-fold>

    //<editor-fold des="add or update the posting file"
    /**
     * adding the term for the first time to posting file
     * @param term - the term
     * @param docName - DocID
     * @param howManyTimesAppearedInThisDoc - the number of times that word was in this document
     */
    private void addToPostingFile(String term, String docName, Integer howManyTimesAppearedInThisDoc)
    {
        HashMap<String,Integer> temp = new HashMap<>();
        temp.put(docName,howManyTimesAppearedInThisDoc);
        postingFile.put(term,temp);
    }

    /**
     * This function update the posting file - if we got the same term in same document
     * this function Refers to the situation that we got the term in upper and lower letters in the same document
     * @param term - term we checking
     * @param docName - the doc name we working on
     * @param howManyTimesAppearedInThisDoc - amount of term in document
     */
    private void updatePostingFileIfSameFile(String term, String docName, Integer howManyTimesAppearedInThisDoc)
    {
        Integer numberOfAppearing = postingFile.get(term).get(docName) + howManyTimesAppearedInThisDoc;
        postingFile.get(term).put(docName,numberOfAppearing);
    }

    /**
     * This function update the posting file - if we got the same term in new document
     * only adding to posting file new doc with amount of appearing in this document
     * @param term - term we checking
     * @param docName - the doc name we working on
     * @param howManyTimesAppearedInThisDoc - amount of term in document
     */
    private void updatePostingFileIfNewFile(String term, String docName, Integer howManyTimesAppearedInThisDoc)
    {
        postingFile.get(term).put(docName,howManyTimesAppearedInThisDoc);
    }

    //</editor-fold>

    //<editor-fold des="initialization the posting file">
    /**
     * initialization posting file to free the memory
     */
    public void initNewPostingFile()
    {
        this.postingFile = new HashMap<>();
    }
    //</editor-fold>

    //<editor-fold des="Getters">

    /**
     * @return - Dictionary
     */
    public HashMap<String,String> getDictionary()
    {
        return dictionary;
    }

    /**
     * @return - size of the dictionary
     */
    public int getSizeOfDictionary()
    {
        return dictionary.size();
    }

    /**
     * @return - posting file
     */
    public HashMap<String,HashMap<String,Integer>> getPostingFile()
    {
        return postingFile;
    }

    /**
     * @return - size of posting file
     */
    public int getSizeOfPostingFile()
    {
        return postingFile.size();
    }
    //</editor-fold>

    //<editor-fold des="Setters"
    public void setDictionary(HashMap<String,String> dictionary)
    {
        this.dictionary = dictionary;
    }
    //</editor-fold>
}
