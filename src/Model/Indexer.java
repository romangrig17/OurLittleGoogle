package Model;

import java.util.*;
import java.util.regex.Pattern;

public class Indexer {

    //<editor-fold des="fields">

    //first string - term.
    //second string - "how many times appeared in the Corpus, in how many docs it was,the last doc where its appeared"
    //[term ; _ , _ , _ ]
    private HashMap<String, String> dictionary;

    //first string - term
    //second string - "DOCID # how many times in that doc"
    //[term ; DOCID # _ , DOCID # _ ...]
    private HashMap<String, HashMap<String,Integer>> postingFile;

    //string - the name of posting ; int - the empty line in the posting;
    //private HashMap<String, Integer> theNamesOfPostingFiles;

    HashSet<String> wordsToUpdateFromUpperToLower;

    //</editor-fold>


    public Indexer() {
        this.dictionary = new HashMap<>();
        this.postingFile = new HashMap<>();
        this.wordsToUpdateFromUpperToLower = new HashSet<>();
        //this.theNamesOfPostingFiles = new HashMap<>();
    }

    public HashMap<String, HashMap<String,Integer>> getPostingFileFromListOfTerms(HashMap<String, Integer> listOfTerms, String docName)
    {
        for (String term: listOfTerms.keySet())
        {
            //only for now
            if (term.length() == 0)
            {
                continue;
            }
            //make all the term with upper letters if the first letter is upper
            String originalTerm = term;
            boolean ifTheWordIsWithUpperLetters = false;
            if (originalTerm.charAt(0) >= 'A' && originalTerm.charAt(0) <= 'Z' && !(Pattern.matches("[_ .,-;'/\t]",originalTerm)))
            {
                originalTerm = originalTerm.toUpperCase();
                ifTheWordIsWithUpperLetters = true;
            }


            if(dictionary.containsKey(originalTerm.toLowerCase()))
            {
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
                        //it its the same document
                        String[] splitedDic = dictionary.get(originalTerm.toLowerCase()).split(",");
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
                    else//here
                    {
                        String[] splitedDic = dictionary.get(originalTerm.toLowerCase()).split(",");
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
                    String[] splitedDic = dictionary.get(originalTerm.toLowerCase()).split(",");
                    Integer updateNumberOfTimesInAllCorpus = (Integer.parseInt(splitedDic[0])) + listOfTerms.get(term);
                    Integer updateNumberOfDocuments = (Integer.parseInt(splitedDic[1])) + 1;
                    dictionary.put(originalTerm.toUpperCase(),updateNumberOfTimesInAllCorpus + "," + updateNumberOfDocuments + "," +docName);
                    //we got the term with lower letters
                    //only need to update the dictionary and add to posting file!
                    /**
                     * TODO: Posting File!
                     */
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
                if(ifTheWordIsWithUpperLetters)
                {
                    //if the word in dictionary was with upper letters and we got with upper letters too - new doc!
                    String[] splitedDic = dictionary.get(originalTerm).split(",");
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
                        String[] splitedDic = dictionary.get(originalTerm.toUpperCase()).split(",");
                        Integer updateNumberOfTimesInAllCorpus = Integer.parseInt(splitedDic[0]) + listOfTerms.get(term);
                        dictionary.put(originalTerm,updateNumberOfTimesInAllCorpus + "," + splitedDic[1] + "," +docName);
                        if (postingFile.containsKey(originalTerm.toUpperCase()))
                        {
                            //need go te all info from posting and do one new
                            Integer amountBefore = postingFile.get(originalTerm.toUpperCase()).get(docName);
                            Integer thisAmount = listOfTerms.get(term);
                            HashMap infoDocs = postingFile.get(originalTerm.toUpperCase());
                            infoDocs.put(docName,amountBefore+thisAmount);
                            postingFile.put(originalTerm,infoDocs);
                        }
                        else
                        {
                            addToPostingFile(originalTerm,docName,listOfTerms.get(term));
                            //wordsToUpdateFromUpperToLower.add(originalTerm.toUpperCase());
                        }
                    }
                    else
                    {
                        String[] splitedDic = dictionary.get(originalTerm.toUpperCase()).split(",");
                        Integer updateNumberOfTimesInAllCorpus = Integer.parseInt(splitedDic[0]) + listOfTerms.get(term);
                        Integer updateNumberOfDocuments = (Integer.parseInt(splitedDic[1])) + 1;
                        dictionary.put(originalTerm,updateNumberOfTimesInAllCorpus + "," + updateNumberOfDocuments + "," +docName);
                        if (postingFile.containsKey(originalTerm.toUpperCase()))
                        {
                            //need go te all info from posting and do one new
                            HashMap infoDocs = postingFile.get(originalTerm.toUpperCase());
                            infoDocs.put(docName,listOfTerms.get(term));
                            postingFile.put(originalTerm,infoDocs);
                        }
                        else
                        {
                            addToPostingFile(originalTerm,docName,listOfTerms.get(term));
                            //wordsToUpdateFromUpperToLower.add(originalTerm.toUpperCase());
                        }
                    }
                    //if the term we got now with lower letters and we got with upper
                    //need to update and dic
                    //
                    /**
                     * TODO: new list for terms that was in the last posting file and we updated the dictionary
                     */

                    postingFile.remove(originalTerm.toUpperCase());
                    dictionary.remove(originalTerm.toUpperCase());
                }
            }
            else
            {
                //if we see the term for the first time
                addToDictionary(originalTerm,listOfTerms.get(term),docName);
                //addToPostingFile(originalTerm,docName,listOfTerms.get(term));
            }
        }// for
        System.out.println("Finish the posting file of: " + docName);
        return postingFile;
    }


    //<editor-fold des="adding to Dictionary,PostingFile and list of posting files>

    //adding to Dictionary and updating the free line if we need.
    //here is the case when the term is not in the dictionary
    private void addToDictionary(String term,Integer howManyTimesAppearedInThisDoc, String docName)
    {
            // if the term is for the first time in the dictionary
            dictionary.put(term, howManyTimesAppearedInThisDoc + ",1," + docName + ",");
            addToPostingFile(term,docName,howManyTimesAppearedInThisDoc);
    }


    /**
     * adding the term for the first time to posting file
     * @param term - the term
     * @param docName - DocID
     * @param howManyTimesAppearedInThisDoc - the number of times that word was in the text
     */
    private void addToPostingFile(String term, String docName, Integer howManyTimesAppearedInThisDoc)
    {
        HashMap<String,Integer> temp = new HashMap<>();
        temp.put(docName,howManyTimesAppearedInThisDoc);
        postingFile.put(term,temp);
    }


    private void updatePostingFileIfSameFile(String term, String docName, Integer howManyTimesAppearedInThisDoc)
    {
        Integer numberOfAppearing = postingFile.get(term).get(docName) + howManyTimesAppearedInThisDoc;
        postingFile.get(term).put(docName,numberOfAppearing);
    }


    private void updatePostingFileIfNewFile(String term, String docName, Integer howManyTimesAppearedInThisDoc)
    {
        postingFile.get(term).put(docName,howManyTimesAppearedInThisDoc);
    }

    public HashSet<String> getWordsToUpdateFromUpperToLower()
    {
        return wordsToUpdateFromUpperToLower;
    }

    public void initWordsToUpdate()
    {
        wordsToUpdateFromUpperToLower = new HashSet<>();
    }
//    private Integer addToPostingName(String term)
//    {
//        int freeLine = 1;
//        //need to check if the word less then 3 chars
//        String substringOfTerm = "";
//        if(term.length() > 3)
//        {
//            substringOfTerm = term.substring(0,3).toLowerCase();
//        }
//        else
//        {
//            substringOfTerm = term.toLowerCase();
//        }
//
//        if(checkIfThePostingFileExisted(substringOfTerm))
//        {
//            freeLine = theNamesOfPostingFiles.get(substringOfTerm) +1 ;
//        }
//        theNamesOfPostingFiles.put(substringOfTerm,freeLine);
//        return freeLine;
//    }
    //</editor-fold>

    //<editor-fold des="many checks">

//    private boolean checkIfThePostingFileExisted(String substringOfTerm)
//    {
//        if(theNamesOfPostingFiles.containsKey(substringOfTerm))
//        {
//            return true;
//        }
//        return false;
//    }

    //</editor-fold>

    //<editor-fold dex="init the posting file">
    public void initNewPostingFile()
    {
        this.postingFile = new HashMap<>();
        //this.dictionary = new HashMap<>();
    }
    //</editor-fold>

    //<editor-fold dex="Getters">
    private String[] getDictionaryInformationOnTerm(String term)
    {
        return dictionary.get(term).split(",");
    }

    public HashMap<String,String> getDictionary()
    {
        return dictionary;
    }

    public int getSizeOfDictionary()
    {
        return dictionary.size();
    }

    public HashMap<String,HashMap<String,Integer>> getPostingFile()
    {
        return postingFile;
    }

    public int getSizeOfPostingFile()
    {
        return postingFile.size();
    }
    //</editor-fold>

}
