import View.GUI;
import ViewModel.Manager;


public class Main {

    public static void main(String[] args) {
        //GUI gui = new GUI();

        Manager manager = new Manager();
        manager.setStemming(false);
        manager.setPathForCorpus("D:\\corpus\\corpus2");
        //manager.setPathForCorpus("D:\\corpus2");
        manager.setPathForPostingFile("D:\\corpus\\Posting");
        manager.run();
        //manager.loadDictionary(false);
        //manager.searchQuery("coffee break");
        
    }
}