import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;


import static org.junit.jupiter.api.Assertions.*;

public class POTest {
    @Test
    public void testCountVotes() {
        //Example data created to use for testing
        String electionName = "Test";
        List<String> candidateNames = Arrays.asList("Candidate A", "Candidate B", "Candidate C", "Candidate D", "Candidate E", "Candidate F");
        int numberOfCandidates = candidateNames.size();
        int numberOfBallots = 9;
        List<String> ballots = Arrays.asList(
                "1,,,,,",
                "1,,,,,",
                ",1,,,,",
                ",,,,1,",
                ",,,,,1",
                ",,,1,,",
                ",,,1,,",
                "1,,,,,",
                ",1,,,,"
        );

        //Create ElectionData instance
        POClass.ElectionData electionData = new POClass.ElectionData(electionName, candidateNames, numberOfCandidates, numberOfBallots, ballots);

        //Call the countVotes() method
        electionData.countVotes();

        //Verify the candidateVotes map contains the expected vote counts
        Map<String, Integer> expectedCandidateVotes = new HashMap<>();
        expectedCandidateVotes.put("Candidate A", 3);
        expectedCandidateVotes.put("Candidate B", 2);
        expectedCandidateVotes.put("Candidate C", 0);
        expectedCandidateVotes.put("Candidate D", 2);
        expectedCandidateVotes.put("Candidate E", 1);
        expectedCandidateVotes.put("Candidate F", 1);

        assertEquals(expectedCandidateVotes, electionData.getCandidateVotes());
    }

    @Test
    public void testDetermineWinnerNoTies() {
        //Example data created to use for testing
        Map<String, Integer> candidateVotes = new HashMap<>();
        candidateVotes.put("Candidate A", 5);
        candidateVotes.put("Candidate B", 3);
        candidateVotes.put("Candidate C", 4);
        candidateVotes.put("Candidate D", 2);
        candidateVotes.put("Candidate E", 1);
        candidateVotes.put("Candidate F", 0);


        //Call the determineWinner() method
        String winner = POClass.ElectionData.determineWinner(candidateVotes);

        //Verify the winner is one of the candidates with the highest votes
        assertTrue(candidateVotes.containsKey(winner));
        assertEquals(5, candidateVotes.get(winner).intValue());
    }
    @Test
    public void testDetermineWinnerTies() {
        //Example data created to use for testing
        Map<String, Integer> candidateVotes = new HashMap<>();
        candidateVotes.put("Candidate A", 5);
        candidateVotes.put("Candidate B", 3);
        candidateVotes.put("Candidate C", 5);
        candidateVotes.put("Candidate D", 2);
        candidateVotes.put("Candidate E", 1);
        candidateVotes.put("Candidate F", 0);


        //Call the determineWinner() method
        String winner = POClass.ElectionData.determineWinner(candidateVotes);

        //Verify the winner is one of the candidates with the highest votes
        assertTrue(candidateVotes.containsKey(winner));
        assertEquals( 5, candidateVotes.get(winner).intValue());
    }

    @Test
    public void testProcessElection() {
        //Example data created to use for testing
        String electionName = "Test Election";
        List<String> candidateNames = Arrays.asList("Candidate A", "Candidate B", "Candidate C", "Candidate D", "Candidate E", "Candidate F");
        int numberOfCandidates = candidateNames.size();
        int numberOfBallots = 9;
        List<String> ballots = Arrays.asList(
                "1,,,,,",
                "1,,,,,",
                ",1,,,,",
                ",,,,1,",
                ",,,,,1",
                ",,,1,,",
                ",,,1,,",
                "1,,,,,",
                ",1,,,,"
        );

        //Create ElectionData instance
        POClass.ElectionData electionData = new POClass.ElectionData(electionName, candidateNames, numberOfCandidates, numberOfBallots, ballots);

        //Call the processElection() method
        electionData.processElection();

        //Verify the winner is set and printed
        assertNotNull(electionData.getWinner());
        System.out.println("Winner: " + electionData.getWinner());
    }
    @Test
    public void testProduceAuditFile() throws IOException {
        //Example data created to use for testing
        String electionName = "Test Election";
        List<String> candidateNames = Arrays.asList("Candidate A", "Candidate B", "Candidate C", "Candidate D", "Candidate E", "Candidate F");
        int numberOfCandidates = candidateNames.size();
        int numberOfBallots = 9;
        List<String> ballots = Arrays.asList(
                "1,,,,,",
                "1,,,,,",
                ",1,,,,",
                ",,,,1,",
                ",,,,,1",
                ",,,1,,",
                ",,,1,,",
                "1,,,,,",
                ",1,,,,"
        );
        String filePath = "audit.txt";

        //Create ElectionData instance
        POClass.ElectionData electionData = new POClass.ElectionData(electionName, candidateNames, numberOfCandidates, numberOfBallots, ballots);

        //Call the processElection() method to determine the winner
        electionData.processElection();

        //Call the produceAuditFile() method
        electionData.produceAuditFile(filePath);

        //Read the content of the audit file
        List<String> fileContent = Files.readAllLines(Paths.get(filePath));

        //Verify the content of the audit file
        assertEquals("Election Name: " + electionName, fileContent.get(0));
        assertEquals("Candidate Names: " + String.join(", ", candidateNames), fileContent.get(1));
        assertEquals("Number of Candidates: " + numberOfCandidates, fileContent.get(2));
        assertEquals("Number of Ballots: " + numberOfBallots, fileContent.get(3));
        assertEquals("", fileContent.get(4));
        assertEquals("Vote Counts:", fileContent.get(5));
        assertEquals("Winner: " + electionData.getWinner(), fileContent.get(6));

        //Verify the vote counts of each candidate in the audit file
        int lineIndex = 7;
        for (Map.Entry<String, Integer> entry : electionData.getCandidateVotes().entrySet()) {
            String expectedLine = entry.getKey() + ": " + entry.getValue();
            assertEquals(expectedLine, fileContent.get(lineIndex));
            lineIndex++;
        }
    }
}
