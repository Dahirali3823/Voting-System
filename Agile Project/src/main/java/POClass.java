//This class handles the PO Class case, it has the logic and algorithm needed to run this specific type of election, it is called in the main file and ran there.
//Author: Matt

import java.util.*;
import java.io.*;
/**
 This class carries out the operations for processing a popularity-only election in a voting system.
 */
public class POClass {
    public static class ElectionData {
        /**
         Represents the data for an election.
         */
        private String electionName;
        private List<String> candidateNames;
        private int numberOfCandidates;
        private int numberOfBallots;
        private List<String> ballots;
        private Map<String, Integer> candidateVotes;
        private Map<String, Integer> results;
        private String winner;
        private boolean count_votes_called;

        /**
         Constructs an ElectionData object with the provided parameters.
         @param electionName the name of the election
         @param candidateNames the list of candidate names
         @param numberOfCandidates the number of candidates
         @param numberOfBallots the number of ballots
         @param ballots the list of ballot strings
         */
        public ElectionData(String electionName, List<String> candidateNames, int numberOfCandidates, int numberOfBallots, List<String> ballots) {
            this.electionName = electionName;
            this.candidateNames = candidateNames;
            this.numberOfCandidates = numberOfCandidates;
            this.numberOfBallots = numberOfBallots;
            this.ballots = ballots;
            this.candidateVotes = new HashMap<>();
            this.results = new HashMap<>();
        }

        /**
         Constructs an ElectionData object by reading data from a file.
         @param file the path to the file containing the election data
         @throws IOException if an I/O error occurs while reading the file
         */
        public ElectionData(String file) throws IOException{
            String electionName;
            List<String> candidateNames = new ArrayList<>();
            int numberOfBallots;
            List<String> ballots = new ArrayList<>();

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                electionName = reader.readLine();
                int numberOfCandidates = Integer.parseInt(reader.readLine());
                // Changed this because it gave us issues when it runs; old code: String[] candidateNamesArray = reader.readLine().split("], [")
                String[] candidateNamesArray = reader.readLine().split("\\], \\[");
                candidateNames.addAll(Arrays.asList(candidateNamesArray).subList(0, numberOfCandidates));
                numberOfBallots = Integer.parseInt(reader.readLine());

                String line;
                while ((line = reader.readLine()) != null) {
                    ballots.add(line.trim());
                }
            }

            this.electionName = electionName;
            this.candidateNames = candidateNames;
            this.numberOfBallots = numberOfBallots;
            this.ballots = ballots;
            this.candidateVotes = new HashMap<>();
            this.results = new HashMap<>();
        }

        /**
         Counts the votes from the provided ballots and updates the candidateVotes map.
         */
        public void countVotes() {

            // Initialize the vote count map
            for (String candidateName : candidateNames) {
                candidateVotes.put(candidateName, 0);
            }

            for (String ballot : ballots) {
                String[] votes = ballot.split(",");
                for (int i = 0; i < votes.length; i++) {
                    if ("1".equals(votes[i])) {
                        String candidateName = candidateNames.get(i);
                        candidateVotes.put(candidateName, candidateVotes.get(candidateName) + 1);
                        break;
                    }
                }
            }
            count_votes_called = true;
        }

        /**
         Processes the election by calling countVotes if necessary, determines the election results,
         determines the winner, and prints the winner's name to the console.
         */
        public void processElection() {
            if(!count_votes_called){
                countVotes();
            }

            //Determine election results
            for (Map.Entry<String, Integer> entry : candidateVotes.entrySet()) {
                String candidate = entry.getKey();
                int votes = entry.getValue();

                results.put(candidate, votes);
            }

            //Calculate winner
            winner = determineWinner(candidateVotes);
            System.out.println("Candidate with most votes: " + winner + " has won the election.");
        }

        /**
         Determines the winner based on the given candidateVotes map.
         @param candidateVotes the map of candidate votes
         @return the name of the winner candidate
         */
        public static String determineWinner(Map<String, Integer> candidateVotes) {
            int max_votes = Integer.MIN_VALUE;
            int countMax = 0;
    
            for (int votes : candidateVotes.values()) {
                if (votes > max_votes) {
                    max_votes = votes;
                    countMax = 1;
                } else if (votes == max_votes) {
                    countMax++;
                }
            }
    
            if (countMax == 1) {
                //If there's only one candidate with the highest number of votes then we determine them as winner
                for (Map.Entry<String, Integer> entry : candidateVotes.entrySet()) {
                    if (entry.getValue() == max_votes) {
                        return entry.getKey();
                    }
                }
            } else {
                //If there are multiple candidates with the highest votes then we perform a coin toss
                Random random = new Random();
                String[] candidates = new String[countMax];
                int index = 0;
    
                for (Map.Entry<String, Integer> entry : candidateVotes.entrySet()) {
                    if (entry.getValue() == max_votes) {
                        candidates[index] = entry.getKey();
                        index++;
                    }
                }
    
                while (candidates.length > 1) {
                    // Generate random indices for the candidates
                    int randomIndex1 = random.nextInt(candidates.length);
                    int randomIndex2 = random.nextInt(candidates.length - 1);

                    // Adjust the second index if it is equal to or greater than the first index
                    if (randomIndex2 >= randomIndex1) {
                        randomIndex2++;
                    }
                    // Simulate a fair coin toss
                    int coinTossResult = random.nextInt(2);
                    // Remove one of the candidates based on the coin toss result
                    if (coinTossResult == 0) {
                        candidates = removeCandidate(candidates, randomIndex2);
                    } else {
                        candidates = removeCandidate(candidates, randomIndex1);
                    }
                }
    
                return candidates[0];
            }
            //If no winner
            return null;
        }

        /**
         Removes a candidate from the candidates array at the specified index.
         @param candidates the array of candidates
         @param index the index of the candidate to remove
         @return the updated array of candidates
         */
        private static String[] removeCandidate(String[] candidates, int index) {
            String[] updatedCandidates = new String[candidates.length - 1];
            int newIndex = 0;
    
            for (int i = 0; i < candidates.length; i++) {
                if (i != index) {
                    updatedCandidates[newIndex] = candidates[i];
                    newIndex++;
                }
            }
    
            return updatedCandidates;
        }

        /**
         Produces an audit file with the election data and results.
         @param filePath the path to the audit file
         @throws IOException if an I/O error occurs while writing the file
         */
        public void produceAuditFile(String filePath)
                throws IOException {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                writer.write("Election Name: " + electionName + "\n");
                writer.write("Candidate Names: " + String.join(", ", candidateNames) + "\n");
                writer.write("Number of Candidates: " + numberOfCandidates + "\n");
                writer.write("Number of Ballots: " + numberOfBallots + "\n");
                writer.write("\nVote Counts:\n");
                writer.write("Winner: " + winner + "\n");

                for (Map.Entry<String, Integer> entry : candidateVotes.entrySet()) {
                    writer.write(entry.getKey() + ": " + entry.getValue() + "\n");
                }
            }
        }

        /**
         Returns the name of the election.
         @return the election name
         */
        public String getElectionName() {
            return electionName;
        }

        /**
         Sets the name of the election.
         @param electionName the new election name
         */
        public void setElectionName(String electionName) {
            this.electionName = electionName;
        }

        /**
         * Returns the list of candidate names.
         *
         * @return the list of candidate names
         */
        public List<String> getCandidateNames() {
            return candidateNames;
        }

        /**
         * Sets the list of candidate names.
         *
         * @param candidateNames the new list of candidate names
         */
        public void setCandidateNames(List<String> candidateNames) {
            this.candidateNames = candidateNames;
        }

        /**
         * Returns the number of ballots.
         *
         * @return the number of ballots
         */
        public int getNumberOfBallots() {
            return numberOfBallots;
        }

        /**
         * Sets the number of ballots.
         *
         * @param numberOfBallots the new number of ballots
         */
        public void setNumberOfBallots(int numberOfBallots) {
            this.numberOfBallots = numberOfBallots;
        }

        /**
         * Returns the list of ballot strings.
         *
         * @return the list of ballot strings
         */
        public List<String> getBallots() {
            return ballots;
        }

        /**
         * Sets the list of ballot strings.
         *
         * @param ballots the new list of ballot strings
         */
        public void setBallots(List<String> ballots) {
            this.ballots = ballots;
        }

        /**
         * Returns the map of candidate votes.
         *
         * @return the map of candidate votes
         */
        public Map<String, Integer> getCandidateVotes() {
            return candidateVotes;
        }

        /**
         * Sets the map of candidate votes.
         *
         * @param candidateVotes the new map of candidate votes
         */
        public void setCandidateVotes(Map<String, Integer> candidateVotes) {
            this.candidateVotes = candidateVotes;
        }

        /**
         * Returns the map of election results.
         *
         * @return the map of election results
         */
        public Map<String, Integer> getResults() {
            return results;
        }

        /**
         * Sets the map of election results.
         *
         * @param results the new map of election results
         */
        public void setResults(Map<String, Integer> results) {
            this.results = results;
        }

        /**
         * Sets the winner of the election.
         *
         * @param winner the name of the winner candidate
         */
        public void setWinner(String winner) {
            this.winner = winner;
        }

        /**
         * Returns the winner of the election.
         *
         * @return the name of the winner candidate
         */
        public String getWinner() {
            return winner;
        }    
    }

}
