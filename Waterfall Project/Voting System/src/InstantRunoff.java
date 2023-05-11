import java.util.*;
import java.io.*;
/*
For a specific election, an instant-runoff voting system is implemented by the Java class InstantRunoff.
All the information required to conduct the election is contained in a nested class called ElectionData.
Two constructors are available for the ElectionData class: one that accepts all the data required to conduct the election and another that accepts the path to a file containing the election data.
The instant-runoff voting algorithm is run by the runIR() method, which then returns the election's victor.
The produceAuditFile() method generates a text file that records the election's specifics, including the number of votes cast for each candidate.
In the event of a tie, the fairCoinToss() helper method conducts a fair coin toss between the candidates. 
 */
public class InstantRunoff {
    public static class ElectionData {
        private List<String[]> ballots;
        private String electionName;
        private List<String> candidateNames;
        private int numberOfCandidates;
        private int numberOfBallots;
        private Map<String, Integer> candidateVotes;
        private String winner;

        public ElectionData(String electionName, int numberOfCandidates, List<String> candidateNames, int numberOfBallots, List<String[]> ballots) {
            this.electionName = electionName;
            this.candidateNames = candidateNames;
            this.numberOfCandidates = numberOfCandidates;
            this.numberOfBallots = numberOfBallots;
            this.ballots = ballots;
            this.candidateVotes = new HashMap<>();
        }

        public ElectionData(String file) throws IOException {
            String electionName;
            List<String> candidateNames = new ArrayList<>();
            int numberOfCandidates;
            int numberOfBallots;
            List<String[]> ballots = new ArrayList<>();

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                electionName = reader.readLine();
                numberOfCandidates = Integer.parseInt(reader.readLine());
                String[] candidateNamesArray = reader.readLine().split(" ");
                candidateNames.addAll(Arrays.asList(candidateNamesArray).subList(0, numberOfCandidates));
                numberOfBallots = Integer.parseInt(reader.readLine());

                String line;
                while ((line = reader.readLine()) != null) {
                    String[] ballot = line.trim().split(" ");
                    ballots.add(ballot);
                }
            }

            this.electionName = electionName;
            this.candidateNames = candidateNames;
            this.numberOfCandidates = numberOfCandidates;
            this.numberOfBallots = numberOfBallots;
            this.ballots = ballots;
            this.candidateVotes = new HashMap<>();

        }
        
        public String runIR() {
            Map<String, Integer> candidateVotes = new HashMap<>();
            for (String[] ballot : ballots) {
                String topCandidate = ballot[0];
                candidateVotes.put(topCandidate, candidateVotes.getOrDefault(topCandidate, 0) + 1);
            }
        
            while (true) {
                String candidateWithLeastVotes = null;
                List<String> leastVotedCandidates = new ArrayList<>();
                for (String candidate : candidateVotes.keySet()) {
                    if (candidateWithLeastVotes == null
                            || candidateVotes.get(candidate) < candidateVotes.get(candidateWithLeastVotes)) {
                        candidateWithLeastVotes = candidate;
                        leastVotedCandidates.clear();
                        leastVotedCandidates.add(candidate);
                    } else if (candidateVotes.get(candidate) == candidateVotes.get(candidateWithLeastVotes)) {
                        leastVotedCandidates.add(candidate);
                    }
                }
        
                if (candidateVotes.get(candidateWithLeastVotes) > numberOfBallots / 2) {
                    this.winner = candidateWithLeastVotes;
                    break;
                } else if (leastVotedCandidates.size() == numberOfCandidates) {
                    // All candidates have the same number of votes
                    this.winner = fairCoinToss(leastVotedCandidates);
                    break;
                } else if (leastVotedCandidates.size() == 1) {
                    // Only one candidate has the least votes
                    candidateWithLeastVotes = leastVotedCandidates.get(0);
                    for (String[] ballot : ballots) {
                        if (ballot[0].equals(candidateWithLeastVotes)) {
                            for (int i = 1; i < numberOfCandidates; i++) {
                                String candidate = ballot[i];
                                if (!candidate.equals(candidateWithLeastVotes)) {
                                    candidateVotes.put(candidate, candidateVotes.getOrDefault(candidate, 0) + 1);
                                    break;
                                }
                            }
                        }
                    }
                    candidateVotes.remove(candidateWithLeastVotes);
                } else {
                    // Multiple candidates have the least votes - fair coin toss
                    String candidateToEliminate = fairCoinToss(leastVotedCandidates);
                    leastVotedCandidates.remove(candidateToEliminate);
                    for (String candidate : leastVotedCandidates) {
                        candidateVotes.remove(candidate);
                    }
                }
            }
        
            if (this.winner == null) {
                // No candidate won a majority in IR, fall back to popularity voting
                Map<String, Integer> candidatePopularity = new HashMap<>();
                for (String[] ballot : ballots) {
                    for (int i = 0; i < numberOfCandidates; i++) {
                        String candidate = ballot[i];
                        candidatePopularity.put(candidate, candidatePopularity.getOrDefault(candidate, 0) + 1);
                    }
                }
                String candidateWithMostVotes = null;
                for (String candidate : candidatePopularity.keySet()) {
                    if (candidateWithMostVotes == null
                            || candidatePopularity.get(candidate) > candidatePopularity.get(candidateWithMostVotes)) {
                        candidateWithMostVotes = candidate;
                    }
                }
                this.winner = candidateWithMostVotes;
            }
        
            return this.winner;
        }

        private String fairCoinToss(List<String> candidates) {
            Random random = new Random();
            int index = random.nextInt(candidates.size());
            return candidates.get(index);
        }
        public String getWinner(){
            return this.winner;
        }
        
        public void produceAuditFile(String filePath)
                    throws IOException {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                    writer.write("Election Name: " + electionName + "\n");
                    writer.write("Name of Candidates: " + String.join(", ", candidateNames) + "\n");
                    writer.write("Number of Candidates: " + numberOfCandidates + "\n");
                    writer.write("Number of Ballots: " + numberOfBallots + "\n");
                    writer.write("\nVote Counts:\n");

                    for (Map.Entry<String, Integer> entry : candidateVotes.entrySet()) {
                        writer.write(entry.getKey() + ": " + entry.getValue() + "\n");
                    }

                    writer.write("\nWinner:\n" + getWinner());
                }
            }
        }
}