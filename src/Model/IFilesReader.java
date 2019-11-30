package Model;


public interface IFilesReader {

    void ReadAll();
    void WriteToFile(StringBuilder document,String path);
}
