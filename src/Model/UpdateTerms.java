package Model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;

public class UpdateTerms {

    public UpdateTerms(String path ,HashSet<String> setOfWords,HashMap<String, HashMap<String,Integer>> postingFile)
    {
        /**
         * TODO: do by the hashcode of term
         */
        for (String wordToFind : setOfWords)
        {
            Integer hashCodeOfWord = wordToFind.hashCode()%250;
            String pathToReadFrom = path + "\\" + hashCodeOfWord +".txt";
            File file = new File(pathToReadFrom);
            StringBuilder docInfoText = new StringBuilder();
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String st;
                String docName = "";
                while ((st = br.readLine()) != null) {
                    //gets the name of the text

                    if (st.split(":")[0].equals(wordToFind))
                    {
                        String allInfo = st.split(":")[1];
                        String[] infoByDocument = allInfo.split(",");
                        for (String infoPerDoc : infoByDocument)
                        {
                            docName = infoPerDoc.split("#")[0];
                            Integer amount = Integer.parseInt(infoPerDoc.split("#")[1]);
                            postingFile.get(wordToFind.toLowerCase()).put(docName,amount);
                        }
                    }
                    else
                    {
                        docInfoText.append(st);
                    }
                }
                br.close();
            }
            catch (Exception e) {
                System.out.println("problem with the reading from file!! in: " + file.getPath());
            }
            wtireTheNewFileWithoutTheWord(pathToReadFrom,docInfoText);
        }
    }

    private void wtireTheNewFileWithoutTheWord(String path, StringBuilder docInfo)
    {
        File file = new File(path);
        /**
         * TODO:check what faster, update or delete and write new
         */
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(docInfo.toString());
            writer.close();
        }
        catch (Exception e)
        {
            System.out.println("Problem To Update The File: path: " + path +", The Text: " + docInfo);
        }
    }
}
