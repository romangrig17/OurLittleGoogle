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
    WriteDictionary writeDictionary;

    //variables
    String pathForPostingFile;
    String pathForCorpus;
    ArrayList<String> allFiles;
    String[][] sortedDictionary;
    //StringBuilder docsInfo;
    int counterOfDocs = 0;
    boolean stemming;

    private static final int AMOUNT_OF_DOCS_IN_POSTING_FILE = 17000;

    public Manager() {
        indexer = new Indexer();
        writeDictionary = new WriteDictionary();
    }

    public void run() {
        //docsInfo = new StringBuilder();
        fileReader = new ReadFile(pathForCorpus);
        allFiles = fileReader.getAllFiles();
        this.pathForPostingFile = pathForPostingFile;
        if (stemming) {
            this.pathForPostingFile = pathForPostingFile + "\\With Stemming";
            new File(pathForPostingFile).mkdirs();
            writePostingFile = new WritePostingFile(new StringBuilder(pathForPostingFile));
        } else {
            this.pathForPostingFile = pathForPostingFile + "\\Without Stemming";
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
        long start = System.currentTimeMillis();
        for (String file : allFiles) {
            HashMap<String, StringBuilder> allTextsFromTheFile = fileReader.getTextsFromTheFile(new File(file));
            Parser parser = new Parser();
            Iterator it = allTextsFromTheFile.keySet().iterator();
            for (String docID : allTextsFromTheFile.keySet()) {
                //parsing each doc
                HashMap<String, Integer> listOfTerms = parser.parseDoc(allTextsFromTheFile.get(docID).toString(), docID);
                //remove the stop words from the list of terms which we got from parser
                listOfTerms = stopWords.removeStopWords(listOfTerms);
                //do a stemming for each term in the doc except numbers
                HashMap<String, Integer> listOfTermsAfterStemming = null;
                if (stemming) {
                    Stemmer steaming = new Stemmer();
                    listOfTermsAfterStemming = steaming.Stemmer(listOfTerms);
                    listOfTerms = null;
                }
                //manager send list of terms after/before stemming to indexer
                if (listOfTermsAfterStemming == null) {
                    //getInfoOnDoc(listOfTerms, docID);
                    indexer.getPostingFileFromListOfTerms(listOfTerms, docID);
                } else {
                    //getInfoOnDoc(listOfTermsAfterStemming, docID);
                    indexer.getPostingFileFromListOfTerms(listOfTermsAfterStemming, docID);
                }
                //make a batch of document in posting file, each batch written to disk
                counterOfDocs++;
                if (counterOfDocs == AMOUNT_OF_DOCS_IN_POSTING_FILE) {
                    counterOfDocs = 0;
                    writePostingFile.putPostingFile(indexer.getPostingFile());
                    threadPool.execute(writePostingFile);
                    indexer.initNewPostingFile();
                }
            }
        }
        //if there is more unwritten posting file
        if (counterOfDocs > 0) {
            writePostingFile.putPostingFile(indexer.getPostingFile());
            threadPool.execute(writePostingFile);
        }
        // once you've submitted your last job to the service it should be shut down
        threadPool.shutdown();
        while (!threadPool.isTerminated()) {
        }

        //writing all the entity we got in corpus and check if the appear more then one time
        writePostingFile.writeTheEntity(indexer.getDictionary());

        //writing the dictionary to disk
        writeDictionary.setPathToWrite(pathForPostingFile, stemming, false);
        writeDictionary.run(indexer.getDictionary());

        //sort the dictionary
        sortByTerms();

        //System.out.println("The amount of unique terms: " + indexer.getDictionary().size());

        //calculate the time for program
        long elapsedTime = System.currentTimeMillis() - start;
        double elapsedTimeD = (double) elapsedTime;
        System.out.println("The time of program: " + (elapsedTimeD / 60000) + " Min");
        System.out.println("Im Done Here");
    }

    //<editor-fold des="Help Function For GUI>
//    private void getInfoOnDoc(HashMap<String, Integer> listOfTerms, String docName) {
//        if (listOfTerms != null && listOfTerms.size() > 2) {
//            int counterAmount = 0;
//            for (Integer amount : listOfTerms.values()) {
//                counterAmount = counterAmount + amount;
//            }
//            docsInfo.append("In Doc: ").append(docName).append(" was: ").append(listOfTerms.size()).append(" Terms");
//            docsInfo.append(" ,And the length of document is:").append(counterAmount);
//            String popularTerm = Collections.max(listOfTerms.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey();
//            docsInfo.append(" And the most popular term is: ").append(popularTerm).append(" And he appeared: ").append(listOfTerms.get(popularTerm)).append(" times. \n");
//        }
//    }


    /**
     * return sorted dictionary: [0] - the term, [1] - the info on term
     *
     * @return - sorted dictionary in array
     */
    public String[][] getSortedDictionary() {
        sortByTerms();
        return sortedDictionary;
    }

    /**
     * This function is sorting the dictionary
     */
    public void sortByTerms() {
        HashMap<String, String> dictionary = indexer.getDictionary();
        sortedDictionary = new String[indexer.getSizeOfDictionary()][2];
        // TreeMap to store values of HashMap
        TreeMap<String, String> sorted = new TreeMap<>();

        // Copy all data from hashMap into TreeMap
        sorted.putAll(dictionary);

        // Display the TreeMap which is naturally sorted
        int i = 0;
        for (HashMap.Entry<String, String> entry : sorted.entrySet()) {

            sortedDictionary[i][0] = entry.getKey();
            sortedDictionary[i][1] = entry.getValue().split(",")[0];
            i++;
        }
    }

    /**
     * This function is loading the dictionary to memory
     */
    public void loadDictionary(boolean stemming) {
        writeDictionary.setPathToWrite(pathForPostingFile, stemming, true);
        indexer.setDictionary(writeDictionary.loadDictionary());
    }
    //</editor-fold>

    //<editor-fold des="Setters">

    /**
     * @param path - where we write the posting file
     */
    public void setPathForPostingFile(String path) {
        this.pathForPostingFile = path;
    }

    /**
     * @param stemming - set the stemming option
     */
    public void setStemming(boolean stemming) {
        this.stemming = stemming;
    }

    /**
     * @param path - from where to read our corpus
     */
    public void setPathForCorpus(String path) {
        this.pathForCorpus = path;
    }
    //</editor-fold>

}
