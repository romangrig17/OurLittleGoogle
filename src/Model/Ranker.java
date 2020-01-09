package Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Ranker {

	
    HashMap<String, Document> docsInfo;
    
    public Ranker(HashMap<String, Document> _docsInfo)
    {
    	this.docsInfo=_docsInfo;
    }
    
    //input- query after parse - maps term from query to amount of time he appeared in the query
    //searcherResultes- containing Hash of String - term from query, mapped to Hash of search results :
    //file name and amount of times the term appeared in the file    
    //returns top 50 ranked by order
	public List<String> rank(HashMap<String,Integer> queryAfterParse, HashMap<String,HashMap<String,Integer>> searcherResultes)
	{
		List<String> resultes= new ArrayList<String>();	
		
		
		return resultes;
	}

}
