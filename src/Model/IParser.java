package Model;



import java.util.ArrayList;
import java.util.HashMap;

public interface IParser {

    HashMap<String,Integer> parseDoc(String doc_Text, String doc_Number);

}
