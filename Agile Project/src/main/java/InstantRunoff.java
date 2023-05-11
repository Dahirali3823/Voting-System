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
        /**
         * The ElectionData class contains all the information required to conduct an election.
         */
        public static class ElectionData {
            private List<String[]> ballots;
            private String electionName;
            private List<String> candidateNames;
            private int numberOfCandidates;
            private int numberOfBallots;
            private Map<String, Integer> candidateVotes;
            private String winner;
            /**
             * Constructs an ElectionData object with the provided data.
             *
             * @param electionName       the name of the election
             * @param numberOfCandidates the number of candidates in the election
             * @param candidateNames     the names of the candidates
             * @param numberOfBallots    the total number of ballots
             * @param ballots            the list of ballots
             */
            public ElectionData(String electionName, int numberOfCandidates, List<String> candidateNames, int numberOfBallots, List<String[]> ballots) {
                this.electionName = electionName;
                this.candidateNames = candidateNames;
                this.numberOfCandidates = numberOfCandidates;
                this.numberOfBallots = numberOfBallots;
                this.ballots = ballots;
                this.candidateVotes = new HashMap<>();
            }
            /**
             * Constructs an ElectionData object by reading data from a file.
             *
             * @param file the path to the file containing the election data
             * @throws IOException if an error occurs while reading the file
             */
            public ElectionData(String file) throws IOException {
                String electionName;
                List<String> candidateNames = new ArrayList<>();
                int numberOfCandidates;
                int numberOfBallots;
                List<String[]> ballots = new ArrayList<>();

                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    electionName = reader.readLine();
                    numberOfCandidates = Integer.parseInt(reader.readLine());
                    String[] candidateNamesArray = reader.readLine().split(","); // Changed to comma-separated
                    candidateNames.addAll(Arrays.asList(candidateNamesArray).subList(0, numberOfCandidates));
                    numberOfBallots = Integer.parseInt(reader.readLine());

                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] ballot = line.trim().split(","); // Changed to comma-separated
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

            /**
             * Runs the instant-runoff voting algorithm and returns the winner of the election.
             *
             * @return the winner of the election
             */
        public String runIR() {
            for (String[] ballot : ballots) {
                for (int i = 0; i < ballot.length; i++) {
                    if (!ballot[i].isEmpty()) {
                        int candidateIndex = Integer.parseInt(ballot[i]) - 1;
                        ballot[i] = candidateNames.get(candidateIndex);
                    } else {
                        ballot[i] = null;
                    }
                }
            }

            Map<String, Integer> candidateVotes = new HashMap<>();
            for (String[] ballot : ballots) {
                String topCandidate = Arrays.stream(ballot).filter(Objects::nonNull).findFirst().orElse(null);
                if (topCandidate != null) {
                    candidateVotes.put(topCandidate, candidateVotes.getOrDefault(topCandidate, 0) + 1);
                }
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
                } else {
                    String candidateToEliminate = leastVotedCandidates.size() == 1 ? candidateWithLeastVotes : fairCoinToss(leastVotedCandidates);

                    for (String[] ballot : ballots) {
                        int index = Arrays.asList(ballot).indexOf(candidateToEliminate);
                        if (index != -1) {
                            for (int i = index; i < ballot.length - 1 && i + 1 < ballot.length; i++) {
                                ballot[i] = ballot[i + 1];
                            }
                            ballot[ballot.length - 1] = null;
                        }
                    }
                    candidateVotes.remove(candidateToEliminate);
                }

                // Check if there are no candidates left
                if (candidateVotes.isEmpty()) {
                    break;
                }
            }

            if (this.winner == null) {
                // No candidate won a majority in IR, fall back to popularity voting
                Map<String, Integer> candidatePopularity = new HashMap<>();
                for (String[] ballot : ballots) {
                    for (String candidate : ballot) {
                        if (candidate != null) {
                            candidatePopularity.put(candidate, candidatePopularity.getOrDefault(candidate, 0) + 1);
                        }
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
        /**
         * Conducts a fair coin toss between the candidates in case of a tie.
         *
         * @param candidates the list of candidates involved in the tie
         * @return the candidate chosen as the winner by the coin toss
         */
        private String fairCoinToss(List<String> candidates) {
            Random random = new Random();
            int index = random.nextInt(candidates.size());
            return candidates.get(index);
        }
        /**
         * Returns the winner of the election.
         *
         * @return the winner of the election
         */
        public String getWinner(){
            return this.winner;
        }
        /**
         * Generates an audit file that records the specifics of the election, including the number of votes cast for each candidate.
         *
         * @param filePath the path to the file where the audit file will be created
         * @throws IOException if an error occurs while writing the audit file
         */
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