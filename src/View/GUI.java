package View;

import ViewModel.Manager;

//org.apache.commons.io.FileUtils.cleanDirectory
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.logging.FileHandler;

public class GUI {

    String inputPath;
    String outputPath;
    boolean stemming;

    Manager manager;

    /**
     * TODO: on Zero button - check the path and throw exception if needed
     */

    public GUI() {
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

    private void placeComponents(JPanel panel)
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

        //setInputs(inputText.toString(),outputText.toString());


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
                    inputPath = fileChooser.getSelectedFile().toString();
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
                    outputPath = fileChooser.getSelectedFile().toString();
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
                File file = new File(outputPath);
                File[] files = file.listFiles();
                if(files!=null) { //some JVMs return null for empty dirs
                    for(File f: files) {
                        f.delete();
                    }
                }
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
                JOptionPane.showMessageDialog(showDicButton,(manager.getSortedDictionary()));
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

        JButton startButton = new JButton("Start!");
        startButton.setBounds(280,80,80,25);
        panel.add(startButton);

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("here!");
                stemming = stemmingCheckBox.isSelected();
                try {
                    if (inputPath == null || outputPath == null)
                    {
                        throw new NullPointerException("The input or output path is empty!");
                    }
                    else
                    {
                        manager = new Manager(inputPath,outputPath,stemming);
                    }
                }catch (NullPointerException e1)
                {
                    e1.toString();
                }
            }
        });

        panel.add(loadDicButton);


    }
}


