package Model;

import javafx.util.Pair;

import java.util.ArrayList;

public interface Parse {

    ArrayList<Pair<String,String>> Parser(String doc_Text, String doc_Name,int doc_Number);

}
