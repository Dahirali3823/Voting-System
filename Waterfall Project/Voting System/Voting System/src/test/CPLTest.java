import org.junit.jupiter.api.Test;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.stat.inference.ChiSquareTest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;




import static org.junit.jupiter.api.Assertions.*;

public class CPLTest {
    @Test
    void testCalculateSeatDistribution() {
        Map<String, Integer> partyVotes = new HashMap<>();
        partyVotes.put("PartyA", 5000);
        partyVotes.put("PartyB", 3000);
        partyVotes.put("PartyC", 1000);
        partyVotes.put("PartyD", 1000);

        int totalSeats = 10;

        CPLClass.ElectionData data = new CPLClass.ElectionData("CPL",
                partyVotes.keySet().stream()
                        .toList(), totalSeats,
                10000, new ArrayList<>());
        data.setPartyVotes(partyVotes);
        data.processElection();
        int[] seats = new int[] { 5, 3, 1, 1 };
        int[] actSeats = data.getResults().values().stream().mapToInt(Integer::intValue).toArray();
        int totalAssignedSeats = data.getResults().values().stream().mapToInt(Integer::intValue).sum();

        assertArrayEquals(seats, actSeats, "The seats are not being correctly distributed");
        assertEquals(totalSeats, totalAssignedSeats, "The total assigned seats should match the total available seats");
    }
    
    @Test
    void testBreakTiesRandomlyEmptyList() {
        List<Map.Entry<String, Integer>> tiedParties = new ArrayList<>();
        Map<String, Integer> partyVotes = new HashMap<>();

        CPLClass.ElectionData data = new CPLClass.ElectionData("CPL", partyVotes.keySet().stream().toList(), 10,
                10000, new ArrayList<>());
        data.setPartyVotes(partyVotes);

        List<Map.Entry<String, Integer>> sortedTiedParties = data.breakTiesRandomly(tiedParties, partyVotes);

        assertTrue(sortedTiedParties.isEmpty(), "The sorted tied parties list should be empty");
    }

    @Test
    void testBreakTiesRandomlySingleParty() {
        List<Map.Entry<String, Integer>> tiedParties = List.of(Map.entry("PartyA", 100));
        Map<String, Integer> partyVotes = new HashMap<>();
        partyVotes.put("PartyA", 5000);

        CPLClass.ElectionData data = new CPLClass.ElectionData("CPL", partyVotes.keySet().stream().toList(), 10,
                10000, new ArrayList<>());
        data.setPartyVotes(partyVotes);

        List<Map.Entry<String, Integer>> sortedTiedParties = data.breakTiesRandomly(tiedParties, partyVotes);

        assertEquals(tiedParties, sortedTiedParties,
                "The sorted tied parties list should be equal to the input tied parties list");
    }
    
    @Test
    void testBreakTiesRandomlyRandomness() {
        List<Map.Entry<String, Integer>> tiedParties = List.of(
                Map.entry("PartyA", 100),
                Map.entry("PartyB", 100),
                Map.entry("PartyC", 100));

        Map<String, Integer> partyVotes = new HashMap<>();
        partyVotes.put("PartyA", 5000);
        partyVotes.put("PartyB", 3000);
        partyVotes.put("PartyC", 1000);

        CPLClass.ElectionData data = new CPLClass.ElectionData("CPL", partyVotes.keySet().stream().toList(), 10,
                8000, new ArrayList<>());
        data.setPartyVotes(partyVotes);
        int numTrials = 100000;
        double significanceLevel = 0.001;
        Map<String, Integer> winCounts = new HashMap<>();
        for (String party : partyVotes.keySet()) {
            winCounts.put(party, 0);
        }

        for (int i = 0; i < numTrials; i++) {
            List<Map.Entry<String, Integer>> sortedTiedParties = data.breakTiesRandomly(tiedParties, partyVotes);
            String winner = sortedTiedParties.get(0).getKey();
            winCounts.put(winner, winCounts.get(winner) + 1);
        }

        long[] observed = winCounts.values().stream().mapToLong(Integer::intValue).toArray();
        double[] expected = new double[winCounts.size()];
        Arrays.fill(expected, (double) numTrials / winCounts.size());

        ChiSquareTest chiSquareTest = new ChiSquareTest();
        double chiSquare = chiSquareTest.chiSquare(expected, observed);
        ChiSquaredDistribution chiSquaredDistribution = new ChiSquaredDistribution(winCounts.size() - 1);
        double pValue = 1 - chiSquaredDistribution.cumulativeProbability(chiSquare);

        System.out.println("p-value: " + pValue);
        assertTrue(pValue >= significanceLevel,
                "The p-value should be greater than or equal to the significance level");
    }

    @Test
    void testReadFileAndPrintVariables() throws IOException {
        String filePath = "dummy_election_data.txt";

        CPLClass.ElectionData electionData = CPLClass.readElectionData(filePath);

        System.out.println("Election Name: " + electionData.electionName);
        System.out.println("Number of Parties: " + electionData.numberOfParties);
        System.out.println("Party Names: " + Arrays.toString(List.toArray(electionData.partyNames)));
        System.out.println("Number of Seats: " + electionData.numberOfSeats);
        System.out.println("Number of Ballots: " + electionData.numberOfBallots);
        System.out.println("Ballots: " + Arrays.toString(List.toArray(electionData.ballots)));
    }
}