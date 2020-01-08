package ViewModel;


import Model.*;
import javafx.beans.binding.StringBinding;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
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
    
    Searcher searcher;

    //variables
    String pathForPostingFile;
    String pathForCorpus;
    ArrayList<String> allFiles;
    String[][] sortedDictionary;
    HashMap<String,Document> docsInfo;
    int counterOfDocs = 0;
    boolean stemming;

    int line = 1;

    private static final int AMOUNT_OF_DOCS_IN_POSTING_FILE = 25000;

    public Manager() {
        indexer = new Indexer();
        writeDictionary = new WriteDictionary();
    }

    public void run() {
        docsInfo = new HashMap<String,Document>();
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
                HashMap<String, Integer> listOfTerms = parser.parseDoc(allTextsFromTheFile.get(docID).toString(), docID);

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
        writeInfoOnDocs();
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

    //key = doc ID , lower and upper words ,  remove ! from entity
    private void getInfoOnDoc(HashMap<String, Integer> listOfTerms, String docName) {
        if (listOfTerms != null && listOfTerms.size() > 2) {
            int counterAmount = 0;
            for (Integer amount : listOfTerms.values()) {
                counterAmount = counterAmount + amount;
            }
            String popularTerm = Collections.max(listOfTerms.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey();
            Document doc=new Document(listOfTerms.size(),counterAmount,popularTerm,listOfTerms.get(popularTerm));
            docsInfo.put(docName,doc);
        }
    }

    private HashMap<String,Document> readInfoOnDocs()
    {
    	HashMap<String,Document> hashDoc=new HashMap<String,Document>();
    	Document doc;
    	
        try{
        	BufferedReader reader = new BufferedReader(new FileReader(pathForPostingFile + "\\InfoOnDocs.txt"));
    	 	String line;
    	 	while((line = reader.readLine()) != null) {
    	 		 String[] splitLine = line.split("#");
    	 		doc=new Document(Integer.parseInt(splitLine[1]),Integer.parseInt(splitLine[2]),splitLine[3],Integer.parseInt(splitLine[4]));
    	 		hashDoc.put(splitLine[0], doc);
             }
        	
    	 	reader.close();
    	 	
        }catch (Exception e)
        {
            System.out.println(e.toString());
        }
        return hashDoc;

    }
    

    private void writeInfoOnDocs()
     {
         try{

        	 	BufferedWriter writer = new BufferedWriter(new FileWriter(pathForPostingFile + "\\InfoOnDocs.txt"));
	         	Set set = docsInfo.entrySet();
	            Iterator iterator = set.iterator();
	            while(iterator.hasNext()) {
	                Map.Entry entry = (Map.Entry)iterator.next();
	                writer.write(entry.getKey()+"#");
	                //System.out.println(entry.getKey());
	                writer.write(docsInfo.get(entry.getKey()).toString());
	               // System.out.println(docsInfo.get(entry.getKey()));
	            }
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
    	if(writeDictionary.pathToWrite() == null)
    	{
    		writeDictionary.setPathToWrite(pathForPostingFile, stemming, true);
    	}
        //indexer.setDictionary(writeDictionary.loadDictionary());
        if(parser==null)
        {
        	System.out.println("pathForCorpus is: "+pathForCorpus);
        	parser= new Parser(pathForCorpus, stemming);
        }
       
        this.pathForPostingFile=writeDictionary.pathToWrite();
        searcher=new Searcher(parser,writeDictionary.loadDictionary(),readInfoOnDocs(),WritePostingFile.AMOUNT_OF_POSTING_FILES,writeDictionary.pathToWrite());
    }
    
    
    public void searchQuery(String query)
    {
    	this.searcher.query(query);
    }

    private HashMap<String, String> updateDictionary(HashMap<String, String> dictionary) {
        HashMap<String, String> updatedDictionary = new HashMap<>();
        Set<String> allTerms = dictionary.keySet();
        for (String term : allTerms) {
            if (term.charAt(0) >= 'A' && term.charAt(0) <= 'Z' && dictionary.containsKey(term.toLowerCase())) {
                String[] splitLineOfUpper = dictionary.get(term).split(",");
                String[] splitLineOfLowwer = dictionary.get(term).split(",");
                int amountOfAppearance = Integer.parseInt(splitLineOfUpper[0]) + Integer.parseInt(splitLineOfLowwer[0]);
                int numberOfDocs = Integer.parseInt(splitLineOfUpper[1]) + Integer.parseInt(splitLineOfLowwer[1]);
                updatedDictionary.put(term.toLowerCase(), amountOfAppearance + "," + numberOfDocs + "," + splitLineOfLowwer[2]);
            } else if(term.charAt(0) == '!'){
                if(Integer.parseInt(dictionary.get(term).split(",")[0]) > 1)
                {
                    updatedDictionary.put(term.substring(1),dictionary.get(term));
                }
            }
            else
            {
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
