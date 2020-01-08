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
    HashMap<String, Document> docsInfo;
	Parser parser;
	int amountOfPostingFiles;
	String pathPostingFiles;
	
	//ctor- receives the same parser from the doc parsing 
	public Searcher(Parser _parser,HashMap<String, String> _dictionary,HashMap<String, Document> _docsInfo, int _amountOfPostingFiles,String _pathPostingFiles)
	{
		this.parser = _parser;
		this.dictionary = _dictionary;
		this.docsInfo=_docsInfo;
		this.amountOfPostingFiles=_amountOfPostingFiles;
		this.pathPostingFiles=_pathPostingFiles;
	}
	
	public void query(String query)
	{
		HashMap<String,Integer> words=parser.parseDoc(query, "0");//word,count in query
		HashMap<String,HashMap<String,Integer>> allInfoPostingFile=new HashMap<String,HashMap<String,Integer>>();//word to hash of <file, count>
		String upperCase,lowerCase;
		
		for (Map.Entry<String,Integer> word: words.entrySet())
		{
			upperCase = word.getKey().toString().toUpperCase();
			lowerCase = word.getKey().toString().toLowerCase();
			System.out.println("searching for:" + word.getKey().toString());
			if(this.dictionary.containsKey(lowerCase))
			{
				Integer termHashCode = (Math.abs((lowerCase).hashCode() % this.amountOfPostingFiles));
				File tempFile = new File(pathPostingFiles+"\\"+termHashCode.toString()+".txt");
				if (tempFile.exists()) 
	            {
					HashMap<String,Integer> infoPostingFile = getTermFromPostingFile(lowerCase, tempFile);//, termsByHashCode.get(termHashCode));
	            	System.out.println("found in lower");
	            	if (infoPostingFile == null)
	                {
	                    System.out.println("term in dict but not in files: " + word.getKey().toString());
	                }
	                else
	                {
	                	allInfoPostingFile.put(lowerCase,infoPostingFile);	
	                }
	            }
			}
			
			if(this.dictionary.containsKey(upperCase))
			{
				Integer termHashCode = (Math.abs((lowerCase).hashCode() % this.amountOfPostingFiles));
				File tempFile = new File(pathPostingFiles+"\\"+termHashCode.toString()+".txt");
				if (tempFile.exists()) 
	            {
					HashMap<String,Integer> infoPostingFile = getTermFromPostingFile(upperCase, tempFile);//, termsByHashCode.get(termHashCode));
	            	System.out.println("found in upper");
	            	if (infoPostingFile == null)
	                {
	                    System.out.println("term in dict but not in files: " + word.getKey().toString());
	                }
	                else
	                {
	                	allInfoPostingFile.put(upperCase,infoPostingFile);	
	                }
	            }
			}
		}
		
		System.out.println("Done- allInfoPostingFile contains results. query is - query input");
	}
	
	public HashMap<String,Integer> getTermFromPostingFile(String term, File file)
	{
		try 
		{
			HashMap<String,Integer> files=new HashMap<String,Integer>();
	        BufferedReader br = new BufferedReader(new FileReader(file));
	        String line;
	        while ((line = br.readLine()) != null) {
	        	if(!line.isEmpty() && term.charAt(0) ==  line.charAt(0))
	        	{
		            String[] lineSplit = line.split(":");
		            if (term.compareTo(lineSplit[0]) == 0)
		            {
		            	String[] docs = lineSplit[1].split(",");
		            	for (String docsLine :docs)
		            	{
		            		String[] docsSplit = docsLine.split("#");
		            		files.put(docsSplit[0],Integer.parseInt(docsSplit[1]));
		            	}
		            	 br.close();
		            	return files;
		            }
	        	}
	        }
	       
	    } 
		catch (Exception e) 
		{
	        e.toString();
	        return null;
	    }
		return null;
	}
	
	

}
