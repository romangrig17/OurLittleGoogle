import View.GUI;
import ViewModel.Manager;


public class Main {

    public static void main(String[] args) {
        //GUI gui = new GUI();
        System.out.println((Math.abs(("POUNDS".toLowerCase()).hashCode() % 1000)));
        System.out.println((Math.abs(("pounds".toLowerCase()).hashCode() % 1000)));
        System.out.println((Math.abs("POUNDS".toLowerCase().hashCode() % 1000)));
        System.out.println((Math.abs("pounds".toLowerCase().hashCode() % 1000)));

        Manager manager = new Manager();
        manager.setStemming(true);
        manager.setPathForCorpus("D:\\My Little Project\\corpus\\corpus");
        manager.setPathForPostingFile("D:\\My Little Project\\PostingFile");
        manager.run();
    }


}
    
    