package ViewModel;


import Model.*;
import javafx.util.Pair;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Manager {


    ReadFile fileReader;
    Indexer indexer;


    ArrayList<String> allFiles;
    StringBuilder sortedDictionary;
    StringBuilder docsInfo;
    int counterOfDocs = 0;

    private static final int AMOUNT_OF_DOCS_IN_POSTING_FILE = 7500;

    public Manager() {
        super();
    }

    public Manager(String pathForCorpus, String pathForPostingFile, boolean stemming) {
        /**
         * TODO: start the gui here and get all params we need. maybe cannot be possible
         * "C:\\My Little Project\\corpus\\corpus"
         * "C:\\My Little Project\\PostingFile"
         */
        docsInfo = new StringBuilder();
        fileReader = new ReadFile(pathForCorpus);
        allFiles = fileReader.getAllFiles();
        indexer = new Indexer();
        WritePostingFile writePostingFile = new WritePostingFile(new StringBuilder(pathForPostingFile));
        /**
         * TODO:Get path for Stop Words
         */
        StopWords stopWords = new StopWords("C:\\My Little Project\\05 stop_words.txt");
        // create a pool of threads, 5 max jobs will execute in parallel
        ExecutorService threadPool = Executors.newFixedThreadPool(5);
        //run on all files
        for (String file : allFiles) {
            HashMap<String, StringBuilder> allTextsFromTheFile = fileReader.getTextsFromTheFile(new File(file));
            Parser parser = new Parser();
            Iterator it = allTextsFromTheFile.keySet().iterator();
            for (String docID : allTextsFromTheFile.keySet()) {
                //<editor-fold> des="Parse"


                /**
                 * Here you do the Parse
                 */
                HashMap<String, Integer> listOfTerms = parser.parseDoc(allTextsFromTheFile.get(docID).toString(), docID);
                //</editor-fold>



                /**
                 * remove the stop words from the list of terms which we got from parser
                 */
                //<editor-fold> des="Stop Words"
                stopWords.removeStopWords(listOfTerms);
                //</editor-fold>

                /**
                 * here is the stemming
                 */
                //<editor-fold> des="Stemming"
                HashMap<String, Integer> listOfTermsAfterStemming = null;
                if (stemming) {
                    Stemmer Stemme = new Stemmer();
                    listOfTermsAfterStemming = Stemme.Stemmer(listOfTerms);
                }
                //</editor-fold>

                /**
                 * here you can delete - this indexing the files
                 */
                //<editor-fold> des="Indexer"
                if (listOfTermsAfterStemming == null) {
                    getInfoOnDoc(listOfTerms, docID);
                    indexer.getPostingFileFromListOfTerms(listOfTerms, docID);
                } else {
                    getInfoOnDoc(listOfTermsAfterStemming, docID);
                    indexer.getPostingFileFromListOfTerms(listOfTermsAfterStemming, docID);
                }
                //</editor-fold>

                /**
                 * Here you can delete - this writing the files
                 */
                counterOfDocs++;
                //<editor-fold des="Writing">
                if (counterOfDocs == AMOUNT_OF_DOCS_IN_POSTING_FILE) {
                    counterOfDocs = 0;
                    writePostingFile.putPostingFile(indexer.getPostingFile());
                    threadPool.execute(writePostingFile);
                    indexer.initNewPostingFile();
                }
                //</editor-fold>
                System.out.println("After one document");

            }
            System.out.println("Done with one file" + file);
        }
        System.out.println("Im Done Here");
        // once you've submitted your last job to the service it should be shut down
        threadPool.shutdown();
        while (!threadPool.isTerminated()) {
        }
    }

    private void getInfoOnDoc(HashMap<String, Integer> listOfTerms, String docName) {
        if (listOfTerms != null && listOfTerms.size() > 2) {
            int counterAmount = 0;
            for (Integer amount : listOfTerms.values()) {
                counterAmount = counterAmount + amount;
            }
            docsInfo.append("In Doc: ").append(docName).append(" was: ").append(listOfTerms.size()).append(" Terms");
            docsInfo.append(" ,And the length of document is:").append(counterAmount);
            String popularTerm = Collections.max(listOfTerms.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey();
            docsInfo.append(" And the most popular term is: ").append(popularTerm).append(" And he appeared: ").append(listOfTerms.get(popularTerm)).append(" times. \n");
        }
    }

    public StringBuilder getSortedDictionary() {
        sortByTerms();
        return sortedDictionary;
    }

    public void sortByTerms() {
        HashMap<String, String> dictionary = indexer.getDictionary();
        sortedDictionary = new StringBuilder();
        // TreeMap to store values of HashMap
        TreeMap<String, String> sorted = new TreeMap<>();

        // Copy all data from hashMap into TreeMap
        sorted.putAll(dictionary);

        // Display the TreeMap which is naturally sorted
        for (HashMap.Entry<String, String> entry : sorted.entrySet()) {
            sortedDictionary.append("Term: ").append(entry.getKey()).append("Amount of performances").append(entry.getValue().split(",")[0]).append("\n");
        }
    }
}
