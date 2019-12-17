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
        docsInfo = new StringBuilder();
        fileReader = new ReadFile(pathForCorpus);
        allFiles = fileReader.getAllFiles();
        indexer = new Indexer();
        WritePostingFile writePostingFile = new WritePostingFile(new StringBuilder(pathForPostingFile));
        for (String file:allFiles)
        {
            HashMap<String,StringBuilder> allTextsFromTheFile = fileReader.getTextsFromTheFile(new File(file));
            Parser parser = new Parser();
            Iterator it = allTextsFromTheFile.keySet().iterator();
            for (String docID: allTextsFromTheFile.keySet())
            {
                /**
                 * TODO: 1) delete the doc name and doc number from params in the parser ; 2) if its a word to check if we need stemming
                 */
                HashMap<String,Integer> listOfTerms = parser.parseDoc(allTextsFromTheFile.get(docID).toString(),docID);
                /**
                 * TODO: remove the stop words.
                 */
                //remove here
                //only remove !! add all before
                //StopWords stopWords = new StopWords("C:\\My Little Project",listOfTerms);

                /**
                 * TODO: stemming!
                 */
                if (stemming == true)
                {
                }
                //getting the information about each document.
                getInfoOnDoc(listOfTerms,docID);
                indexer.getPostingFileFromListOfTerms(listOfTerms,docID);
                //HashSet<String> wordsForUpdate = indexer.getWordsToUpdateFromUpperToLower();
                //UpdateTerms updateTerms = new UpdateTerms(pathForPostingFile,wordsForUpdate,indexer.getPostingFile());
                //indexer.initWordsToUpdate();

                Thread thread;
                writePostingFile.putPostingFile(indexer.getPostingFile());
                thread = new Thread(writePostingFile);
                thread.run();
                thread.getName();
                //writePostingFile.toWrite(indexer.getPostingFile());

                /**
                 * Look init is here! need to change this!
                 */
                indexer.initNewPostingFile();

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
