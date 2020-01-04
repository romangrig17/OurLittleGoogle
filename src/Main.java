import View.GUI;
import ViewModel.Manager;


public class Main {

    public static void main(String[] args) {
        //GUI gui = new GUI();

        Manager manager = new Manager();
        manager.setStemming(false);
        manager.setPathForCorpus("C:\\corpus");
        //manager.setPathForCorpus("D:\\corpus2");
        manager.setPathForPostingFile("C:\\PostingFile");
        manager.run();
    }


}
    
    