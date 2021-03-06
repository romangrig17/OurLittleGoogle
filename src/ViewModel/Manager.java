package ViewModel;


import Model.*;
import Model.Term.*;
import Model.Term.Number;
import javafx.beans.binding.StringBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Manager {


    //classes
    ReadFile fileReader;
    Indexer indexer;
    WritePostingFile writePostingFile;
    WriteDictionary writeDictionary;
    Parser parser;

   // Searcher searcher;

    //variables
    String pathForPostingFile;
    String pathForCorpus;
    ArrayList<String> allFiles;
    String[][] sortedDictionary;
    StringBuilder docsInfo;
    int counterOfDocs = 0;
    boolean stemming;

    HashMap<String,Integer> infoDocHsh = new HashMap<>();
    int line = 1;

    private static final int AMOUNT_OF_DOCS_IN_POSTING_FILE = 20000;

    public Manager() {
        indexer = new Indexer();
        writeDictionary = new WriteDictionary();
    }

    public void run() {
        docsInfo = new StringBuilder();
        fileReader = new ReadFile(pathForCorpus);
        allFiles = fileReader.getAllFiles();
        if (stemming) {
            this.pathForPostingFile = pathForPostingFile + "\\With Stemming";
            new File(pathForPostingFile).mkdirs();
            writePostingFile = new WritePostingFile(pathForPostingFile);
        } else {
            this.pathForPostingFile = pathForPostingFile + "\\Without Stemming";
            File file = new File(pathForPostingFile);
            file.mkdir();
            writePostingFile = new WritePostingFile(pathForPostingFile);
        }
        parser = new Parser(pathForCorpus, stemming);
        // create a pool of threads, 5 max jobs will execute in parallel
        ExecutorService threadPool = Executors.newFixedThreadPool(5);
        //run on all files
        long start = System.currentTimeMillis();
        for (String file : allFiles) {
            HashMap<String, StringBuilder> allTextsFromTheFile = fileReader.getTextsFromTheFile(new File(file));
            for (String docID : allTextsFromTheFile.keySet()) {
                //parsing each doc
                HashMap<String, ITerm> listOfTerms = parser.parseDoc(allTextsFromTheFile.get(docID).toString(), docID);

                //building temp posting file on ram
                indexer.getPostingFileFromListOfTerms(listOfTerms, docID);

                //make a batch of document in posting file, each batch written to disk
                getInfoOnDoc(listOfTerms,docID);
                counterOfDocs++;
                if (counterOfDocs == AMOUNT_OF_DOCS_IN_POSTING_FILE) {
                    indexer.setDictionary((indexer.getDictionary()));
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

        //update the dictionary for lower and upper letters in terms
        System.out.println("Before Update");
        indexer.setDictionary(updateDictionary(indexer.getDictionary()));
        System.out.println("After Update");
        //writing all the entity we got in corpus and check if the appear more then one time
        writePostingFile.writeTheEntity(indexer.getDictionary());
        //writing the dictionary to disk
        writeDictionary.setPathToWrite(pathForPostingFile, stemming, false);
        writeDictionary.run(indexer.getDictionary());

        //sort the dictionary
        sortByTerms();

        System.out.println("The amount of unique terms: " + indexer.getDictionary().size());
        writeInfoOnDoc();
        writeInfoOnDocHash();
        //calculate the time for program
        long elapsedTime = System.currentTimeMillis() - start;
        double elapsedTimeD = (double) elapsedTime;
        System.out.println("The time of program: " + (elapsedTimeD / 60000) + " Min");

        /*
        final Map<String, Integer> sortedByCount = sortByValue(getHelpDic());
        int i=0;
        for (String term: sortedByCount.keySet())
        {
            i++;
            if(i>10)
            {
                break;
            }
            else {
                System.out.println(term + " : " + indexer.getDictionary().get(term).split(",")[0]);
            }
        }
        */
    }

    //<editor-fold des="Help Function For GUI>

    //key = doc ID , lower and upper words
    private void getInfoOnDoc(HashMap<String, ITerm> listOfTerms, String docName) {
        if (listOfTerms != null && listOfTerms.size() > 2) {
            int counterAmount = 0;
            Iterator it = listOfTerms.values().iterator();
            while(it.hasNext())
            {
                counterAmount = counterAmount + ((ITerm)it.next()).getNumOfAppearanceInCorpus();
            }
            infoDocHsh.put(docName,line);
            docsInfo.append(docName).append(":Unique Terms: ").append(listOfTerms.size()).append(" ,Words: ").append(counterAmount).append(";");
            Map.Entry<String, ITerm> maxEntry = null;

            for (Map.Entry<String, ITerm> entry : listOfTerms.entrySet())
            {
                if (maxEntry == null || (entry.getValue().getNumOfAppearanceInDocs()) > (maxEntry.getValue().getNumOfAppearanceInDocs()))
                {
                    maxEntry = entry;
                }
            }
            String popularTerm = maxEntry.getKey();
            docsInfo.append(popularTerm).append("#").append(listOfTerms.get(popularTerm).getNumOfAppearanceInDocs()).append("\n");
            line++;
            //docsInfo.append("In Doc: ").append(docName).append(" was: ").append(listOfTerms.size()).append(" Terms");
            //docsInfo.append(" ,And the length of document is:").append(counterAmount);
            //String popularTerm = Collections.max(listOfTerms.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey();
            //docsInfo.append(" And the most popular term is: ").append(popularTerm).append(" And he appeared: ").append(listOfTerms.get(popularTerm)).append(" times. \n");
        }
    }


    private void writeInfoOnDocHash()
    {
        try{

            FileOutputStream file = new FileOutputStream(pathForPostingFile + "\\Info On Docs Hash.txt");
            ObjectOutputStream oos = new ObjectOutputStream(file);

            oos.writeObject(infoDocHsh);
            oos.close();
         	
             /*File file = new File(pathForPostingFile + "\\Info On Docs Hash.txt");
             FileWriter writer = new FileWriter(file);
             writer.write(infoDocHsh.toString());
             writer.close();*/
        }catch (Exception e)
        {
            e.toString();
        }

    }



    private void writeInfoOnDoc()
    {
        try{
            File file = new File(pathForPostingFile + "\\Info On Docs.txt");
            FileWriter writer = new FileWriter(file);
            writer.write(docsInfo.toString());
            writer.close();
        }catch (Exception e)
        {
            e.toString();
        }

    }

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
        HashMap<String, ITerm> dictionary = indexer.getDictionary();
        sortedDictionary = new String[indexer.getSizeOfDictionary()][2];
        // TreeMap to store values of HashMap
        TreeMap<String, ITerm> sorted = new TreeMap<>();

        // Copy all data from hashMap into TreeMap
        sorted.putAll(dictionary);

        // Display the TreeMap which is naturally sorted
        int i = 0;
        for (HashMap.Entry<String, ITerm> entry : sorted.entrySet()) {

            sortedDictionary[i][0] = entry.getKey();
            sortedDictionary[i][1] = Integer.toString(entry.getValue().getNumOfAppearanceInCorpus());
            i++;
        }
    }

    /**
     * This function is loading the dictionary to memory
     */
    public void loadDictionary(boolean stemming) {
        writeDictionary.setPathToWrite(pathForPostingFile, stemming, true);
        //indexer.setDictionary(writeDictionary.loadDictionary());
        if(parser==null)
        {
            System.out.println("pathForCorpus is: "+pathForCorpus);
            parser= new Parser(pathForCorpus, stemming);
        }

        //searcher=new Searcher(parser,writeDictionary.loadDictionary(),writeDictionary.loadDictionaryInfo(),WritePostingFile.AMOUNT_OF_POSTING_FILES,writeDictionary.pathToWrite());
    }

//    public void searchQuery(String query)
//    {
//        this.searcher.query(query);
//    }

    private HashMap<String, ITerm> updateDictionary(HashMap<String, ITerm> dictionary) {
        HashMap<String, ITerm> updatedDictionary = new HashMap<>();
        Set<String> allTerms = dictionary.keySet();
        for (String term : allTerms) {
            if (term.charAt(0) >= 'A' && term.charAt(0) <= 'Z' && dictionary.containsKey(term.toLowerCase())) {
                ITerm tempPtrLow = dictionary.get(term.toLowerCase());
                ITerm tempPtrUpper = dictionary.get(term.toUpperCase());
                int amountOfAppearance = tempPtrUpper.getNumOfAppearanceInCorpus() + tempPtrLow.getNumOfAppearanceInCorpus();
                int numberOfDocs = tempPtrUpper.getNumOfAppearanceInDocs() + tempPtrLow.getNumOfAppearanceInDocs();
                String instance = tempPtrLow.getInstance();
                 if (instance.equals("Number")) {
                    updatedDictionary.put(term.toLowerCase(), new Number(term.toLowerCase(), amountOfAppearance, numberOfDocs, tempPtrLow.getLastDocument()));
                }
                //Expression
                else if (instance.equals("Expression")) {
                    updatedDictionary.put(term.toLowerCase(), new Expression(term.toLowerCase(), amountOfAppearance, numberOfDocs, tempPtrLow.getLastDocument()));
                }
                //Term
                else {
                    updatedDictionary.put(term.toLowerCase(), new Term(term.toLowerCase(), amountOfAppearance, numberOfDocs, tempPtrLow.getLastDocument()));
                }
            } else {
                updatedDictionary.put(term, dictionary.get(term));
            }
        }
        return updatedDictionary;
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

    //<editor-fold des = "sort dictionary by value">
/*
    public HashMap<String , Integer> getHelpDic()
    {
        HashMap<String , Integer> helpDic = new HashMap<>();
        HashMap<String,String> dicHashMap = indexer.getDictionary();

        for(String term : dicHashMap.keySet())
        {
            helpDic.put(term,Integer.parseInt(dicHashMap.get(term).split(",")[0]));
        }

        return helpDic;
    }

    public static Map<String, Integer> sortByValue(final Map<String, Integer> wordCounts) {
        return wordCounts.entrySet()
                .stream()
                .sorted((Map.Entry.<String, Integer>comparingByValue().reversed()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }
 */
    //</editor-fold>
}
