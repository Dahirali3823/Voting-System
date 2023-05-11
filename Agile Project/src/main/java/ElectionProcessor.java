//This is the main file, it creates the GUI and multiple files functionality. ProcessElection has all the calls to the elections
//Authors: Muhsin, Dahir, Julian


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
/**
 * The ElectionProcessor class implements the GuiLogicInterface and serves as the main class for the Election File Reader program.
 * It provides methods for processing elections, managing selected files, and creating the GUI.
 */
public class ElectionProcessor implements GuiLogicInterface {
    private String filename;
    private int numOptions;
    private List<String> parties;
    private int numSeats;
    private int numBallots;
    private List<String[]> IRballots;
    private List<String> CPLballots;
    private List<String> candidates;
    private static String currentElectionType = null;
    private static final List<File> selectedFilesList = new ArrayList<>();
    private static JLabel selectedFilesLabel;
    private static ElectionFileManager electionFileManager;
    /**
     * The main method of the ElectionProcessor class.
     * It starts the GUI by invoking the createAndShowGUI method.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });

    }

    /**
     * Creates and displays the main GUI window with the necessary components.
     */
    private static void createAndShowGUI() {
        electionFileManager = new ElectionFileManager(new ElectionProcessor());
        JFrame frame = new JFrame("Election File Reader");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setSize(500, 300);

        // Create a panel with a title and a custom font
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 20));
        JLabel titleLabel = new JLabel("Election File Reader");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titlePanel.add(titleLabel);
        frame.add(titlePanel, BorderLayout.NORTH);

        // Add a label to display selected files
        selectedFilesLabel = new JLabel();
        selectedFilesLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        frame.add(selectedFilesLabel, BorderLayout.WEST);

        // Create a panel with a styled "Select Files" button
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());
        JPanel buttonPanelDone = new JPanel();
        buttonPanelDone.setLayout(new GridBagLayout());
        JButton openFileButton = new JButton("Select Files");
        openFileButton.setFont(new Font("Arial", Font.PLAIN, 18));
        openFileButton.setPreferredSize(new Dimension(200, 50));

        openFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();

                // Set the default directory to the current directory
                fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));

                fileChooser.setMultiSelectionEnabled(true);
                int returnValue = fileChooser.showOpenDialog(frame);

                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File[] selectedFiles = fileChooser.getSelectedFiles();
                    for (File file : selectedFiles) {

                            processFile(file);

                    }
                }
            }
        });
         /*    JButton doneButton = new JButton("Done");
            doneButton.addActionListener(new ActionListener() {
             @Override
            public void actionPerformed(ActionEvent e) {
            System.out.println(selectedFilesList.toString());
            frame.dispose();
               
                

            }

         });
       */
      JButton doneButton = new JButton("Done");
      doneButton.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
              String files = ElectionProcessor.getFilesListHtml();
              File firstFile = selectedFilesList.get(0);
              String electionType = ElectionFileManager.getFileElectionType(firstFile);
              ElectionProcessor electionProcessor = new ElectionProcessor();
              electionProcessor.processElection(files, electionType);
              frame.dispose();
          }
      });

       



        buttonPanel.add(openFileButton);
        buttonPanelDone.add(doneButton);
        frame.add(buttonPanel, BorderLayout.CENTER);
        frame.add(buttonPanelDone, BorderLayout.SOUTH);


        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * Restarts the application, will be removed in future release as this implementation creates a new window rather than restarting
     */
    @Override
    public void restartApplication() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
    /**
     * Sets the text of the JLabel displaying the list of selected files.
     *
     * @param files A string containing the file names to be displayed.
     */
    @Override
    public void setText(String files) {
        selectedFilesList.add(new File(files));

        selectedFilesLabel.setText("<html>Selected files: " + files + "</html>");
        selectedFilesLabel.revalidate();
        selectedFilesLabel.repaint();
    }
        
    /**
     * Processes the selected file by calling the addFile method in the ElectionFileManager instance.
     * Updates the JLabel displaying the list of selected files.
     *
     * @param file The file to be processed.
     */
   private static void processFile(File file) {
        try {
            String electionType = electionFileManager.addFile(file);

        } catch (IOException ex) {
            // Handle error
        }  
    }


    /**
     * Returns an HTML-formatted string representation of the list of selected files.
     *
     * @return An HTML-formatted string containing the file names.
     * 
     *
     */
    private static String getFilesListHtml() {
        StringBuilder sb = new StringBuilder();
        for (File file : selectedFilesList) {
            sb.append(file.getName()).append(",");
        }
        return sb.toString();
    }
    public void processElection(String files, String electionType) {
        if (electionType != null) {
            if ("CPL".equals(electionType.trim())) {
                String[] fileNames = files.split(",");
                for (String fileName : fileNames) {
                    try {
                        File file = new File(fileName.trim());
                        // Read and process the input file using the constructor that accepts a file
                        CPLClass.ElectionData electionData = new CPLClass.ElectionData(file.getAbsolutePath());
        
                        // Process the election
                        electionData.processElection();
        
                        // Display results
                        Map<String, Integer> WinnerMap = electionData.getResults();
                        for (Map.Entry<String, Integer> entry : WinnerMap.entrySet()) {
                            String party = entry.getKey();
                            int numSeats = entry.getValue();
                            System.out.println(party + ":" + numSeats);
                        }
        
                        // Produce the audit file
                        electionData.produceAuditFile(file.getName() + "_audit.txt");
                    } catch (IOException e) {
                        System.err.println("Error processing CPL election: " + e.getMessage());
                    }
                }
            } else if ("IR".equals(electionType.trim())) {
                // Process IR elections
                String[] fileNames = files.split(",");
                for (String fileName : fileNames) {
                    try {
                        File file = new File(fileName.trim());
                        InstantRunoff.ElectionData electionData = new InstantRunoff.ElectionData(file.getAbsolutePath());
                        electionData.runIR();
                        String winner = electionData.getWinner();
                        System.out.println("The winner is: " + winner);
                        electionData.produceAuditFile(file.getName() + "_audit.txt");
                    } catch (IOException e) {
                        System.out.println("Error reading input file: " + e.getMessage());
                    }
                }
            } else if ("PO".equals(electionType.trim())) {
                // Process PO elections
                String[] fileNames = files.split(",");
                for (String fileName : fileNames) {
                    try {
                        File file = new File(fileName.trim());
                        POClass.ElectionData poElectionData = new POClass.ElectionData(file.getAbsolutePath());
                        poElectionData.processElection();
                        String winner = poElectionData.getWinner();
                        System.out.println("Winner: " + winner);
                        poElectionData.produceAuditFile(file.getName() + "_audit.txt");
                    } catch (IOException e) {
                        System.err.println("Error processing PO election: " + e.getMessage());
                    }
                }
            } else {
                System.out.println("Election Type is null");
                // Handle the case where the election type is not recognized.
            }
        }
    }
    
    

}
