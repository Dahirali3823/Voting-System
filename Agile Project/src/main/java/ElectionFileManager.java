//This file handles the way the files are manipulated within the GUI, its a helper file.

//Author: Julian, Dahir, Muhsin


import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class ElectionFileManager {
    private List<File> selectedFilesList = new ArrayList<>();
    private String currentElectionType = null;

//    private static JLabel guiLogic;
    private final GuiLogicInterface guiLogic;

    public ElectionFileManager(GuiLogicInterface guiLogic) {
        this.guiLogic = guiLogic;
    }

    /**
     * Adds a file to the list of selected files if it has a valid election header.
     * If the file's election type does not match the current election type, prompts the user
     * to choose an action: restart the application or remove files of one of the detected election types.
     *
     * @param file The file to be added.
     * @throws IOException If an I/O error occurs while reading the file or the header is invalid.
     */
    public String addFile(File file) throws IOException {
        if (selectedFilesList.contains(file)) {
            throw new IOException("Duplicate file");
        }
        
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String firstLine = reader.readLine();
        if (firstLine == null || (!firstLine.equals("PO") && !firstLine.equals("IR") && !firstLine.equals("CPL"))) {
            throw new IOException("Invalid file format");
        }
        reader.close();
    
        if (currentElectionType == null) {
            currentElectionType = firstLine;
            selectedFilesList.add(file);
            guiLogic.setText(getFilesListHtml());
        } else if (!currentElectionType.equals(firstLine)) {
            selectedFilesList.add(file);
            guiLogic.setText(getFilesListHtml());
    
            String message = "Files of different types were selected.\n" +
                    "Please select only one type of file (PO, IR, or CPL).";
            JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    
            Object[] options = {"Restart", "Remove Files of One Type"};
            int response = JOptionPane.showOptionDialog(null,
                    "Do you want to restart or remove files of one of the detected elections?",
                    "Select an option",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);
    
            if (response == JOptionPane.YES_OPTION) {
                // Restart the application
                guiLogic.restartApplication();
    
            } else if (response == JOptionPane.NO_OPTION) {
                // Ask the user which type of files they want to remove
                String[] types = {"PO", "IR", "CPL"};
                String selectedType = (String) JOptionPane.showInputDialog(null,
                        "Select the type of files to remove:",
                        "Remove Files",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        types,
                        types[0]);
    
                if (selectedType != null) {
                    // Remove files of the selected type
                    selectedFilesList.removeIf(f -> Objects.equals(getFileElectionType(f), selectedType));
                    currentElectionType = selectedFilesList.isEmpty() ? null : getFileElectionType(selectedFilesList.get(0));
                    guiLogic.setText(getFilesListHtml());
                }
            }
        } else {
            selectedFilesList.add(file);
            guiLogic.setText(getFilesListHtml());
        }
        return firstLine;
    }
    

    /**
     * Returns the election type from the header of the given file.
     *
     * @param file The file to read the election type from.
     * @return The election type as a string (e.g., "PO", "IR", "CPL").
     */
    public static String getFileElectionType(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns the list of currently selected files.
     *
     * @return A list of File objects representing the selected files.
     */


    public List<File> getSelectedFilesList() {
        return selectedFilesList;
    }
    /**
     * Returns an HTML-formatted string representation of the list of selected files.
     *
     * @return An HTML-formatted string containing the file names.
     */
    private String getFilesListHtml() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < selectedFilesList.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            String fileName = selectedFilesList.get(i).getName();
            if (!sb.toString().contains(fileName)) {
                sb.append(fileName);
            }
        }
        return sb.toString();
    }
    
}
