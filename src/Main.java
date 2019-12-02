import Model.FilesReader;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField; 


public class Main {

    public static void main(String[] args)
    {
        System.out.println("Hello World!");

        long start = System.currentTimeMillis();

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Runnable test = new FilesReader("C:\\Users\\user1\\Desktop\\masters\\השלמה\\information_retrieval\\corpus\\corpus_test");
        
        //((FilesReader) test).GetListOfDirs();
        
        for(int i=0; i<1 ; i++)
        {
            executorService.execute(test);
        }
        
        executorService.shutdown();

        while (!executorService.isTerminated()){}
        
        long elapsedTime = System.currentTimeMillis() - start;
        System.out.println("The time of program: " + elapsedTime);
    }
    
    
    
    private static void placeComponents(JPanel panel) 
    {   
        panel.setLayout(null);
        // Creating JLabel
        JLabel inputLabel = new JLabel("Input path:");
        /* This method specifies the location and size
         * of component. setBounds(x, y, width, height)
         * here (x,y) are cordinates from the top left 
         * corner and remaining two arguments are the width
         * and height of the component.
         */
        inputLabel.setBounds(10,20,80,25);
        panel.add(inputLabel);

        // Creating text field for input
        JTextField inputText = new JTextField(20);
        inputText.setBounds(100,20,165,25);
        panel.add(inputText);

        // Same process for output field.
        JLabel outputLabel = new JLabel("Output path:");
        outputLabel.setBounds(10,50,80,25);
        panel.add(outputLabel);

        
        JTextField outputText = new JTextField(20);
        outputText.setBounds(100,50,165,25);
        panel.add(outputText);
        
        JButton inputBrowseButton = new JButton("Browse");
        inputBrowseButton.setBounds(280,20,80,25);
        panel.add(inputBrowseButton);
             
        // Creating input browse button

        inputBrowseButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            JFileChooser fileChooser = new JFileChooser();
     
            // For Directory
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
     
            // For File
            //fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
     
            fileChooser.setAcceptAllFileFilterUsed(false);
     
            int rVal = fileChooser.showOpenDialog(null);
            if (rVal == JFileChooser.APPROVE_OPTION) 
            {
              inputText.setText(fileChooser.getSelectedFile().toString());
            }
          }
        });
      
        
        // Creating input browse button
        
        JButton outputBrowseButton = new JButton("Browse");
        outputBrowseButton.setBounds(280,50,80,25);
        panel.add(outputBrowseButton);
             
        outputBrowseButton.addActionListener(new ActionListener() 
        {
          public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
     
            // For Directory
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
     
            // For File
            //fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
     
            fileChooser.setAcceptAllFileFilterUsed(false);
     
            int rVal = fileChooser.showOpenDialog(null);
            if (rVal == JFileChooser.APPROVE_OPTION) 
            {
              outputText.setText(fileChooser.getSelectedFile().toString());
            }
          }
        });
        
        
        
        // Creating stemming checkBox
        JCheckBox stemmingCheckBox = new JCheckBox("Allow stemming");
        stemmingCheckBox.setBounds(10, 80, 120, 25);
        panel.add(stemmingCheckBox);
        
        // Creating zero button
        JButton zeroButton = new JButton("Zero");
        zeroButton.setBounds(10, 120, 80, 25);
        zeroButton.addActionListener(new ActionListener()
        {
        	@Override
			public void actionPerformed(ActionEvent e) 
			{
			// TODO zero action
			}
		});
        
        panel.add(zeroButton);
        
        // Creating showDic button
        JButton showDicButton = new JButton("Show dictionary");
        showDicButton.setBounds(110, 120, 125, 25);
        showDicButton.addActionListener(new ActionListener() 
        {
			@Override
			public void actionPerformed(ActionEvent e)
			{
			// TODO show dictionary action
			}
		});
		     
        panel.add(showDicButton);
		        
		// Creating loadDic button
		JButton loadDicButton = new JButton("Load dictionary");
        loadDicButton.setBounds(255, 120, 125, 25);
        loadDicButton.addActionListener(new ActionListener()
        {		
			@Override
			public void actionPerformed(ActionEvent e) 
			{
			// TODO load dictionary action
			
			}
		});
        
        panel.add(loadDicButton);
    }
    
    
    public static void main2(String[] args) {    
        // Creating instance of JFrame
        JFrame frame = new JFrame("Our Little Google");
        // Setting the width and height of frame
        frame.setSize(430,230);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        /* Creating panel. This is same as a div tag in HTML
         * We can create several panels and add them to specific 
         * positions in a JFrame. Inside panels we can add text 
         * fields, buttons and other components.
         */
        JPanel panel = new JPanel();    
        // adding panel to frame
        frame.add(panel);
        /* calling user defined method for adding components
         * to the panel.
         */
        placeComponents(panel);

        // Setting the frame visibility to true
        frame.setVisible(true);
    }
}
    
    