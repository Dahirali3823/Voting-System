import java.util.*;
import java.io.*;

/*
Processing elections for the two types of "CPL" and "IR" elections is the responsibility of this class, ElectionProcessor.
The required data is read from a CSV file with the default name input.csv, validated, and then the user is prompted for the information before the election is processed.
Information about the election is stored in a number of private instance variables that belong to the class.
These elements are the following initilizations.
 */
public class ElectionProcessor {
    private String fileName;
    private String electionType;
    private int numOptions;
    private List<String> parties;
    private int numSeats;
    private int numBallots;
    private List<String[]> IRballots;
    private List<String> CPLballots;
    private List<String> candidates;

    public ElectionProcessor() {
        fileName = "input.csv";
    }

    public static void main(String[] args) {
        ElectionProcessor processor = new ElectionProcessor();                                                    //creates an instance of the ElectionProcessor class
        List<String> fileContents = processor.parseFile();                                                        //calls the parseFile() method on the processor object to read in the input file and store its contents in a list
        String[] requiredInfo = {"fileName", "electionType", "numOptions", "parties", "numSeats", "numBallots"}; //creates an array of strings requiredInfo containing the names of the information fields that are required for the election
        String[] missingItems = new String[0];                                                                  //initializes an empty string array missingItems to store the names of any missing information fields
        if ("CPL".equals(fileContents.get(0))) {
            missingItems = processor.validateInfoCPL(fileContents, requiredInfo);
        } else if ("IR".equals(fileContents.get(0))) {
            missingItems = processor.validateInfoIR(fileContents, requiredInfo);
        }

        if (missingItems.length > 0) {
            processor.notEnoughInfo(missingItems);
        } else {
            //processor.promptUser(requiredInfo);
            processor.processElection();
        }
    }
  /*      ElectionProcessor processor = new ElectionProcessor();
        List<String> fileContents = processor.parseFile();
        String[] requiredInfo = {"fileName", "electionType", "numOptions", "parties", "numSeats", "numBallots"};
        String[] missingItems = new String[0];
        if (fileContents.get(0) == "CPL") {
            String[] missingItem = processor.validateInfoCPL(fileContents, requiredInfo);
        }
        else if (fileContents.get(0) == "IR") {
            String[] missingItem = processor.validateInfoIR(fileContents, requiredInfo);
        }

        if (missingItems.length > 0) {
            processor.notEnoughInfo(missingItems);
        } else {
            processor.promptUser(requiredInfo);
            processor.processElection();
        }
    } */
    
    /*
    This method processes the election by creating an instance of the appropriate class (CPLClass or InstantRunoff), passing the necessary data to it, and calling its methods to produce the election's results and audit file
     */
  /*  public void processElection() {
        if(electionType == null){
            System.out.println("error : returned null");
        }
        if (electionType == "CPL") { //It first verifies that the election type is "CPL". 
            CPLClass.ElectionData electionData = new CPLClass.ElectionData(electionType, parties, numSeats, numBallots, CPLballots); //If so, the method creates a CPLClass instance.
            try {
                electionData.produceAuditFile(fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (electionType == "IR") {
            InstantRunoff.ElectionData electionData = new InstantRunoff.ElectionData(electionType, numOptions, candidates, numBallots, IRballots);
            electionData.runIR();
            electionData.getWinner();
            try {
                electionData.produceAuditFile(fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }*/
    public void processElection() {
        if (electionType == null) {
            System.out.println("Error: returned null");
        }
     if (electionType.equals("CPL")) {
            CPLClass.ElectionData electionData = new CPLClass.ElectionData(electionType, parties, numSeats, numBallots, CPLballots);
            System.out.println("Election type: " + electionType);
            System.out.println("Parties: " + String.join(", ", parties));
            System.out.println("Number of seats: " + numSeats);
            System.out.println("Number of ballots: " + numBallots);
            try {
                electionData.produceAuditFile(fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (electionType.equals("IR")) {
            InstantRunoff.ElectionData electionData = new InstantRunoff.ElectionData(electionType, numOptions, candidates, numBallots, IRballots);
            System.out.println("Election type: " + electionType);
            System.out.println("Number of candidates: " + numOptions);
            System.out.println("Candidates: " + String.join(", ", candidates));
            System.out.println("Number of ballots: " + numBallots);
            electionData.runIR();
            String winner = electionData.getWinner();
            System.out.println("Winner: " + winner);
            try {
                electionData.produceAuditFile(fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*
    The CSV (comma-separated values) file's contents are read by the method parseFile(), which returns the contents as a list of strings. 
    */
    public List<String> parseFile() {
    List<String> fileContents = new ArrayList<>();
    try {
        Scanner scanner = new Scanner(new File(fileName));
        while (scanner.hasNextLine()) {
            fileContents.add(scanner.nextLine());
        }
        scanner.close();
    } catch (FileNotFoundException e) {
        System.out.println("Error: File not found");
        e.printStackTrace();
    }
    return fileContents;
}
    /*
    This method validates the required information for a CPL election and returns an array of missing information.
     */
    public String[] validateInfoCPL(List<String> fileContents, String[] requiredInfo) {
        if (fileContents.size() < 5) {
            return requiredInfo;
        }

        electionType = fileContents.get(0);
        numOptions = Integer.parseInt(fileContents.get(1));
        parties = Arrays.asList(fileContents.get(2).split(","));
        numSeats = Integer.parseInt(fileContents.get(3));
        numBallots = Integer.parseInt(fileContents.get(4));
        CPLballots = new ArrayList<>();
        for (int i = 5; i < fileContents.size(); i++) {
            CPLballots.add(fileContents.get(i));
        }

        return new String[0];
    }
    /*
    This method validates the required information for an IR election and returns an array of missing information.
     */
    public String[] validateInfoIR(List<String> fileContents, String[] requiredInfo) {
        if (fileContents.size() < 5) {
            return requiredInfo;
        }

        electionType = fileContents.get(0);
        numOptions = Integer.parseInt(fileContents.get(1));
        candidates = Arrays.asList(fileContents.get(2).split(","));
        numBallots = Integer.parseInt(fileContents.get(3));

        IRballots = new ArrayList<>();
        for (int i = 4; i < fileContents.size(); i++) {
            String[] ballotStr = fileContents.get(i).split(",");
            IRballots.add(ballotStr);
        }

        return new String[0];
    }
    /*
    This method prompts the user with the information for the election.
     */
    public void promptUser(String[] requiredInfo) {
        System.out.println("Processing the election with the following information:");
        System.out.println("Election type: " + electionType);
        System.out.println("Number of Candidates: " + numOptions);
        if ("CPL".equals(electionType)) {
        System.out.println("Parties: " + String.join(", ", parties));
        System.out.println("Number of seats: " + numSeats);
        }
    } 

    public void notEnoughInfo(String[] missingItems) {
        System.out.println("Error: Missing information in the input file.");
        System.out.println("Missing items: " + String.join(", ", missingItems));
    }
}