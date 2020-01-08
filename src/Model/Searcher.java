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
		HashMap<String,Integer> words=parser.parseDoc(query, "0");
		HashSet<String> allInfoPostingFile=new HashSet<String>();
		for (Map.Entry word: words.entrySet())
		{
			System.out.println("searching for:" + word.getKey().toString());
			if(this.dictionary.containsKey(word.getKey().toString().toLowerCase()))
			{
				Integer termHashCode = (Math.abs((word.getKey().toString().toLowerCase()).hashCode() % this.amountOfPostingFiles));
				File tempFile = new File(pathPostingFiles+"\\"+termHashCode.toString()+".txt");
				if (tempFile.exists()) 
	            {
	            	HashSet<String> infoPostingFile = getTermFromPostingFile(word.getKey().toString().toLowerCase(), tempFile);//, termsByHashCode.get(termHashCode));
	            	System.out.println("found in lower");
	            	if (infoPostingFile == null)
	                {
	                    System.out.println("term in dict but not in files: " + word.getKey().toString());
	                }
	                else
	                {
	                	/*for (String filepost: infoPostingFile)
	                	{
	                		String[] lineSplit = filepost.split("#");
	                		System.out.println("found in :" + lineSplit[0] + " times: "+lineSplit[1]);
	                	}*/
	                	allInfoPostingFile.addAll(infoPostingFile);	
	                }
	            }
			}
			
			if(this.dictionary.containsKey(word.getKey().toString().toUpperCase()))
			{
				Integer termHashCode = (Math.abs((word.getKey().toString().toLowerCase()).hashCode() % this.amountOfPostingFiles));
				File tempFile = new File(pathPostingFiles+"\\"+termHashCode.toString()+".txt");
				if (tempFile.exists()) 
	            {
	            	HashSet<String> infoPostingFile = getTermFromPostingFile(word.getKey().toString().toUpperCase(), tempFile);//, termsByHashCode.get(termHashCode));
	            	System.out.println("found in upper");
	            	if (infoPostingFile == null)
	                {
	                    System.out.println("term in dict but not in files: " + word.getKey().toString());
	                }
	                else
	                {
	                	/*for (String filepost: infoPostingFile)
	                	{
	                		String[] lineSplit = filepost.split("#");
	                		//System.out.println("found in :" + lineSplit[0] + " times: "+lineSplit[1]);
	                	}*/
	                	allInfoPostingFile.addAll(infoPostingFile);	
	                }
	            }
				
			}
		}
	}
	
	public HashSet<String> getTermFromPostingFile(String term, File file)
	{
		try 
		{
			HashSet<String> files=new HashSet<String>();;
	        BufferedReader br = new BufferedReader(new FileReader(file));
	        String line;
	        while ((line = br.readLine()) != null) {
	        	if(!line.isEmpty() && term.charAt(0) ==  line.charAt(0))
	        	{
		            String[] lineSplit = line.split(":");
		            if (term.compareTo(lineSplit[0]) == 0)
		            {
		            	String[] docs = lineSplit[1].split(",");
		            	for (String doc :docs)
		            			files.add(doc);
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
