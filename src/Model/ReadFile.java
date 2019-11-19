package Model;


public interface ReadFile {

    void startToRead();
    void writheFile(StringBuilder document,String path);
    void getListOfDirs(String path);
}
