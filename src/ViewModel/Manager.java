package ViewModel;


import Model.*;
import javafx.util.Pair;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Manager {


    //classes
    ReadFile fileReader;
    Indexer indexer;
    WritePostingFile writePostingFile;


    ArrayList<String> allFiles;
    String[][] sortedDictionary;
    StringBuilder docsInfo;
    int counterOfDocs = 0;

    private static final int AMOUNT_OF_DOCS_IN_POSTING_FILE = 17000;

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
        if (stemming)
        {
            pathForPostingFile = pathForPostingFile +"\\With Stemming";
            new File(pathForPostingFile).mkdirs();

            //file.mkdir();
            writePostingFile = new WritePostingFile(new StringBuilder(pathForPostingFile));
        }
        else
        {
            pathForPostingFile = pathForPostingFile +"\\Without Stemming";
            File file = new File(pathForPostingFile);
            file.mkdir();
            writePostingFile = new WritePostingFile(new StringBuilder(pathForPostingFile));
        }
        /**
         * TODO:Get path for Stop Words
         */
        StopWords stopWords = new StopWords(pathForCorpus);
        // create a pool of threads, 5 max jobs will execute in parallel
        ExecutorService threadPool = Executors.newFixedThreadPool(5);
        //run on all files
        //long start = System.currentTimeMillis();
        for (String file : allFiles) {
            HashMap<String, StringBuilder> allTextsFromTheFile = fileReader.getTextsFromTheFile(new File(file));
            Parser parser = new Parser();
            Iterator it = allTextsFromTheFile.keySet().iterator();
            for (String docID : allTextsFromTheFile.keySet()) {

                /**
                 * Here you do the Parse
                 */
                HashMap<String, Integer> listOfTerms = parser.parseDoc(allTextsFromTheFile.get(docID).toString(), docID);



                /**
                 * remove the stop words from the list of terms which we got from parser
                 */
                listOfTerms = stopWords.removeStopWords(listOfTerms);

                /**
                 * here is the stemming
                 */
                //<editor-fold> des="Stemming"
                HashMap<String, Integer> listOfTermsAfterStemming = null;
                if (stemming) {
                    Stemmer steaming = new Stemmer();
                    listOfTermsAfterStemming = steaming.Stemmer(listOfTerms);
                }

                /**
                 * here you can delete - this indexing the files
                 */
                if (listOfTermsAfterStemming == null) {
                    getInfoOnDoc(listOfTerms, docID);
                    indexer.getPostingFileFromListOfTerms(listOfTerms, docID);
                } else {
                    getInfoOnDoc(listOfTermsAfterStemming, docID);
                    indexer.getPostingFileFromListOfTerms(listOfTermsAfterStemming, docID);
                }

                /**
                 * Here you can delete - this writing the files
                 */
                counterOfDocs++;
                if (counterOfDocs == AMOUNT_OF_DOCS_IN_POSTING_FILE) {
                    counterOfDocs = 0;
                    writePostingFile.putPostingFile(indexer.getPostingFile());
                    threadPool.execute(writePostingFile);
                    //System.out.println(Thread.activeCount());
                    //writePostingFile.run();
                    indexer.initNewPostingFile();
                }
                //System.out.println("After one document");
            }
        }
        //threadPool.shutdown();
        //while (!threadPool.isTerminated()) {}
        //if there is more unwritten files
        if (counterOfDocs > 0)
        {
            writePostingFile.putPostingFile(indexer.getPostingFile());
            threadPool.execute(writePostingFile);
        }
        // once you've submitted your last job to the service it should be shut down
        threadPool.shutdown();
        while (!threadPool.isTerminated()) {}

        //writing all the entity
        writePostingFile.writeTheEntity(indexer.getDictionary());
//        long elapsedTime = System.currentTimeMillis() - start;
//        double elapsedTimeD = (double) elapsedTime;
//        System.out.println("The time of program: " + (elapsedTimeD/60000) + " Min");
        System.out.println("Im Done Here");
        System.out.println("The amount of unique terms: " + indexer.getDictionary().size());
        sortByTerms();

    }

    //<editor-fold des="Help Function For GUI>
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

    public String[][] getSortedDictionary() {
        sortByTerms();
        return sortedDictionary;
    }

    public void sortByTerms() {
        HashMap<String, String> dictionary = indexer.getDictionary();
        sortedDictionary = new String[indexer.getSizeOfDictionary()][2];
        // TreeMap to store values of HashMap
        TreeMap<String, String> sorted = new TreeMap<>();

        // Copy all data from hashMap into TreeMap
        sorted.putAll(dictionary);

        // Display the TreeMap which is naturally sorted
        int i=0;
        for (HashMap.Entry<String, String> entry : sorted.entrySet()) {

            sortedDictionary[i][0] = entry.getKey();
            sortedDictionary[i][1] = entry.getValue();
            i++;
        }
        //System.out.println(sortedDictionary);
    }

    //</editor-fold>
}
