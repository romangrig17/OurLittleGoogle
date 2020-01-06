package Model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Searcher {
		
    /**
     * first string - term.
     * second string - "how many times appeared in the Corpus, in how many docs it was,the last doc where it appeared"
     * [term ; _ , _ , _ ]
     */
    HashMap<String, String> dictionary;
	Parser parser;
	int amountOfPostingFiles;
	String pathPostingFiles;
	
	//ctor- receives the same parser from the doc parsing 
	public Searcher(Parser _parser,HashMap<String, String> _dictionary, int _amountOfPostingFiles,String _pathPostingFiles)
	{
		this.parser = _parser;
		this.dictionary = _dictionary;
		this.amountOfPostingFiles=_amountOfPostingFiles;
		this.pathPostingFiles=_pathPostingFiles;
	}
	
	public void query(String query)
	{
		HashMap<String,Integer> words=parser.parseDoc(query, "0");
		HashSet<String> allInfoPostingFile=new HashSet<String>();
		for (Map.Entry word: words.entrySet())
		{
			System.out.println("searching for:" + word.getKey().toString());
			Integer termHashCode = (Math.abs((word.getKey().toString().toLowerCase()).hashCode() % this.amountOfPostingFiles));
			File tempFile = new File(pathPostingFiles+"\\"+termHashCode.toString()+".txt");
			if (tempFile.exists()) 
            {
            	HashSet<String> infoPostingFile = getTermFromPostingFile(word.getKey().toString(), tempFile);//, termsByHashCode.get(termHashCode));
                if (infoPostingFile == null)
                {
                    System.out.println("term in dict but not in files: " + word.getKey().toString());
                }
                else
                {
                	for (String filepost: infoPostingFile)
                	{
                		String[] lineSplit = filepost.split("#");
                		//System.out.println("found in :" + lineSplit[0] + " times: "+lineSplit[1]);
                	}
                	allInfoPostingFile.addAll(infoPostingFile);	
                }
            } 
		}
	}
	
	public HashSet<String> getTermFromPostingFile(String term, File file)
	{
		try 
		{
			HashSet<String> files;
	        BufferedReader br = new BufferedReader(new FileReader(file));
	        String line;
	        StringBuilder text = new StringBuilder();
	        term= term.toLowerCase();
	        while ((line = br.readLine()) != null) {
	            //if the term is already in the posting file - we will update the line and put the new to
	            String[] lineSplit = line.split(":");
	            if (term.compareTo(lineSplit[0].toLowerCase()) == 0) //TODO: in upper?
	            {
	            	files=new HashSet<String>();
	            	String[] docs = lineSplit[1].split(",");
	            	for (String doc :docs)
	            			files.add(doc);
	            	return files;
	            	 
	            }
	        }
	        br.close();
	        return null;
	    } 
		catch (Exception e) 
		{
	        e.toString();
	        return null;
	    }

	}
	
	

}
