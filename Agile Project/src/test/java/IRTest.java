//Created by Muhsin

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InstantRunoffTest {
    @Test
    public void testRunIR() throws IOException {
        String filePath = "test/TestData/IRTest.csv";
        InstantRunoff.ElectionData electionData = new InstantRunoff.ElectionData(filePath);

        String winner = electionData.runIR();

        // Verify the winner is set and printed
        assertNotNull(winner);
        System.out.println("Winner: " + winner);
    }

    @Test
    public void testProduceAuditFile() throws IOException {
        String filePath = "test/TestData/IRTest.csv";
        InstantRunoff.ElectionData electionData = new InstantRunoff.ElectionData(filePath);

        String winner = electionData.runIR();
        String auditFilePath = "audit.txt";

        electionData.produceAuditFile(auditFilePath);

        // Read the content of the audit file
        List<String> fileContent = Files.readAllLines(Paths.get(auditFilePath));

        // Verify the content of the audit file
        assertEquals("Election Name: " + electionData.electionName, fileContent.get(0));
        assertEquals("Name of Candidates: " + String.join(", ", electionData.candidateNames), fileContent.get(1));
        assertEquals("Number of Candidates: " + electionData.numberOfCandidates, fileContent.get(2));
        assertEquals("Number of Ballots: " + electionData.numberOfBallots, fileContent.get(3));
        assertEquals("", fileContent.get(4));
        assertEquals("Vote Counts:", fileContent.get(5));

        // Verify the vote counts of each candidate in the audit file
        int lineIndex = 6;
        for (Map.Entry<String, Integer> entry : electionData.candidateVotes.entrySet()) {
            String expectedLine = entry.getKey() + ": " + entry.getValue();
            assertEquals(expectedLine, fileContent.get(lineIndex));
            lineIndex++;
        }

        assertEquals("\nWinner:\n" + winner, fileContent.get(lineIndex));
    }
}
