import View.GUI;
import ViewModel.Manager;


public class Main {

    public static void main(String[] args) {
       // GUI gui = new GUI();

        Manager manager = new Manager();
        manager.setStemming(false);
        manager.setPathForCorpus("C:\\Users\\user1\\Desktop\\masters\\השלמה\\information_retrieval\\corpus\\part_of_corpus");
        manager.setPathForPostingFile("C:\\Users\\user1\\Desktop\\masters\\השלמה\\information_retrieval\\corpus\\corpus_write");
        manager.run();
    }


}
    
    