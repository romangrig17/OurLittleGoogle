package ViewModel;


import Model.*;
import javafx.util.Pair;
import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Manager {



    ReadFile fileReader;
    Indexer indexer;


    ArrayList<String> allFiles;
    StringBuilder sortedDictionary;
    StringBuilder docsInfo;

    private static final int AMOUNT_OF_DOCS_IN_POSTING_FILE = 7000;

    public Manager()
    {
        super();
    }

    public Manager(String pathForCorpus, String pathForPostingFile, boolean stemming){
        /**
         * TODO: start the gui here and get all params we need. maybe cannot be possible
         * "C:\\My Little Project\\corpus\\corpus"
         * "C:\\My Little Project\\PostingFile"
         */
        int counterOfDocs =0;
        docsInfo = new StringBuilder();
        fileReader = new ReadFile(pathForCorpus);
        allFiles = fileReader.getAllFiles();
        indexer = new Indexer();
        WritePostingFile writePostingFile = new WritePostingFile(new StringBuilder(pathForPostingFile));
        /**
         * TODO:Get path for Stop Words
         */
        StopWords stopWords = new StopWords("C:\\Users\\user1\\Desktop\\masters\\השלמה\\information_retrieval\\project\\Stop_Words.txt");
        for (String file:allFiles)
        {
            HashMap<String,StringBuilder> allTextsFromTheFile = fileReader.getTextsFromTheFile(new File(file));
            Parser parser = new Parser();
            Iterator it = allTextsFromTheFile.keySet().iterator();
            for (String docID: allTextsFromTheFile.keySet())
            {
                //<editor-fold> des="Parse"
                HashMap<String,Integer> listOfTerms = parser.parseDoc(allTextsFromTheFile.get(docID).toString(),docID);
                //</editor-fold>
                /**
                 * remove the stop words from the list of terms which we got from parser
                 */
                //<editor-fold> des="Stop Words"
                stopWords.removeStopWords(listOfTerms);
                //</editor-fold>

                //<editor-fold> des="Stemming"
                HashMap<String,Integer> listOfTermsAfterStemming =null;
                if (stemming)
                {
                    Stemmer Stemme = new Stemmer();
                    listOfTermsAfterStemming = Stemme.Stemmer(listOfTerms);
                }
                //</editor-fold>

                //<editor-fold> des="Indexer"
                if (listOfTermsAfterStemming == null)
                {
                    getInfoOnDoc(listOfTerms,docID);
                    indexer.getPostingFileFromListOfTerms(listOfTerms,docID);
                }
                else
                {
                    getInfoOnDoc(listOfTermsAfterStemming,docID);
                    indexer.getPostingFileFromListOfTerms(listOfTermsAfterStemming,docID);
                }
                //</editor-fold>





                if(counterOfDocs == AMOUNT_OF_DOCS_IN_POSTING_FILE)
                {
                    counterOfDocs = 0;
                    //<editor-fold> des="Writing"
                    Thread thread;
                    writePostingFile.putPostingFile(indexer.getPostingFile());
                    thread = new Thread(writePostingFile);
                    thread.run();
                    thread.getName();


                    //writePostingFile.toWrite(indexer.getPostingFile());

//                ExecutorService executorService = Executors.newFixedThreadPool(1);
//                Runnable test = new WritePostingFile("C:\\My Little Project\\PostingFile",indexer.getPostingFile());
//               // ((WritePostingFile) test).toWrite();
//
//                  for(int i=0; i<1 ; i++)
//                  {
//                      executorService.execute(test);
//                  }
////
//                  executorService.shutdown();
//                  while (!executorService.isTerminated()){}

                    //</editor-fold>

                    /**
                     * Clean Posting file from the memory
                     */
                    indexer.initNewPostingFile();
                }
                System.out.println("After one document");

            }
            System.out.println("Done with one file" + file);
        }
        System.out.println("Im Done Here");
    }

    private void getInfoOnDoc(HashMap<String,Integer> listOfTerms,String docName)
    {
        docsInfo.append("In Doc: ").append(docName).append(" was: ").append(listOfTerms.size()).append(" Terms");
        String popularTerm = Collections.max(listOfTerms.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey();
        docsInfo.append(" And the most popular term is: ").append(popularTerm).append(" And he appeared: ").append(listOfTerms.get(popularTerm)).append(" times. \n");
    }

    public StringBuilder getSortedDictionary()
    {
        sortByTerms();
        return sortedDictionary;
    }

    public void sortByTerms()
    {
        HashMap<String, String> dictionary = indexer.getDictionary();
        sortedDictionary = new StringBuilder();
        // TreeMap to store values of HashMap
        TreeMap<String, String> sorted = new TreeMap<>();

        // Copy all data from hashMap into TreeMap
        sorted.putAll(dictionary);

        // Display the TreeMap which is naturally sorted
        for (HashMap.Entry<String, String> entry : sorted.entrySet())
        {
            sortedDictionary.append("Term: ").append(entry.getKey()).append("Amount of performances").append(entry.getValue().split(",")[0]).append("\n");
        }
    }
}
