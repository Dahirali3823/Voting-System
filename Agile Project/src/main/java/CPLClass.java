
import java.io.*;
import java.util.*;
/**
 * The CPLClass represents a class that contains the nested ElectionData class.
 */
public class CPLClass {
    /**
     * The ElectionData class represents data related to an election.
     */
    public static class ElectionData {
        private String electionName;
        private List<String> partyNames;
        private int numberOfSeats;
        private int numberOfBallots;
        private List<String> ballots;
        private Map<String, Integer> partyVotes;
        private Map<String, Integer> results;

        private boolean count_votes_called;
        /**
         * Constructs an ElectionData object with the specified parameters.
         *
         * @param electionName     the name of the election
         * @param partyNames       the list of party names
         * @param numberOfSeats    the number of seats
         * @param numberOfBallots  the number of ballots
         * @param ballots          the list of ballots
         */
        public ElectionData(String electionName, List<String> partyNames, int numberOfSeats, int numberOfBallots, List<String> ballots) {
            this.electionName = electionName;
            this.partyNames = partyNames;
            this.numberOfSeats = numberOfSeats;
            this.numberOfBallots = numberOfBallots;
            this.ballots = ballots;
            this.partyVotes = new HashMap<>();
            this.results = new HashMap<>();

        }
        /**
         * Constructs an ElectionData object by reading data from a file.
         *
         * @param file  the path to the file containing election data
         * @throws IOException if an I/O error occurs while reading the file
         */
        public ElectionData(String file) throws IOException{
            String electionName;
            List<String> partyNames = new ArrayList<>();
            int numberOfSeats;
            int numberOfBallots;
            List<String> ballots = new ArrayList<>();

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                electionName = reader.readLine();
                int numberOfParties = Integer.parseInt(reader.readLine());
                String[] partyNamesArray = reader.readLine().split(" ");
                partyNames.addAll(Arrays.asList(partyNamesArray).subList(0, numberOfParties));
                numberOfSeats = Integer.parseInt(reader.readLine());
                numberOfBallots = Integer.parseInt(reader.readLine());

                String line;
                while ((line = reader.readLine()) != null) {
                    ballots.add(line.trim());
                }
            }

            this.electionName = electionName;
            this.partyNames = partyNames;
            this.numberOfSeats = numberOfSeats;
            this.numberOfBallots = numberOfBallots;
            this.ballots = ballots;
            this.partyVotes = new HashMap<>();
            this.results = new HashMap<>();

        }
        /**
         * Counts the votes based on the ballots.
         * This method should be called before calling the processElection method.
         */
        public void countVotes() {

            // Initialize the vote count map
            for (String partyName : partyNames) {
                partyVotes.put(partyName, 0);
            }

            for (String ballot : ballots) {
                String[] votes = ballot.split(",");
                for (int i = 0; i < votes.length; i++) {
                    if ("1".equals(votes[i])) {
                        String partyName = partyNames.get(i);
                        partyVotes.put(partyName, partyVotes.get(partyName) + 1);
                        break;
                    }
                }
            }
            count_votes_called = true;
        }
        /**
         * Processes the election and determines the seat distribution.
         */
        public void processElection() {
            if(!count_votes_called){
                countVotes();
            }
            int totalVotes = partyVotes.values().stream().mapToInt(Integer::intValue).sum();
            Map<String, Integer> seatDistribution = new HashMap<>();

            // Calculate the initial seat distribution based on the proportion of votes
            int remainingSeats = numberOfSeats;
            for (String party : partyVotes.keySet()) {
                int seats = (int) Math.floor((double) partyVotes.get(party) * numberOfSeats / totalVotes);

                results.put(party, seats);
                remainingSeats -= seats;
            }

            // Distribute remaining seats according to the largest remainders
            List<Map.Entry<String, Integer>> remainders = new ArrayList<>();
            for (String party : partyVotes.keySet()) {
                int remainder = partyVotes.get(party) % (totalVotes/numberOfSeats);

                remainders.add(Map.entry(party, remainder));
            }
            List<Map.Entry<String, Integer>> tiedParties = new ArrayList<>();
            int maxRemainder = -1;

            for (Map.Entry<String, Integer> entry : remainders) {
                if (entry.getValue() > maxRemainder) {
                    maxRemainder = entry.getValue();
                    tiedParties.clear();
                    tiedParties.add(entry);
                } else if (entry.getValue() == maxRemainder) {
                    tiedParties.add(entry);
                }
            }

            if (tiedParties.size() > 1) {
                tiedParties = breakTiesRandomly(tiedParties, partyVotes);
            }
            remainders = tiedParties;

            for (int i = 0; i < remainingSeats; i++) {
                String party = remainders.get(i).getKey();
                results.put(party, results.get(party) + 1);
            }

        }
        /**
         * Produces an audit file containing the election details, vote counts, and seat distribution.
         *
         * @param filePath  the path to the output file
         * @throws IOException if an I/O error occurs while writing the file
         */
        public void produceAuditFile(String filePath)
                throws IOException {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                writer.write("Election Name: " + electionName + "\n");
                writer.write("Party Names: " + String.join(", ", partyNames) + "\n");
                writer.write("Number of Seats: " + numberOfSeats + "\n");
                writer.write("Number of Ballots: " + numberOfBallots + "\n");
                writer.write("\nVote Counts:\n");

                for (Map.Entry<String, Integer> entry : partyVotes.entrySet()) {
                    writer.write(entry.getKey() + ": " + entry.getValue() + "\n");
                }

                writer.write("\nSeat Distribution:\n");
                for (Map.Entry<String, Integer> entry : results.entrySet()) {
                    writer.write(entry.getKey() + ": " + entry.getValue() + "\n");
                }
            }
        }

        /**
         * Breaks ties randomly among the tied parties based on their votes.
         *
         * @param tiedParties  the list of tied parties
         * @param partyVotes   the map of party votes
         * @return the list of tied parties sorted randomly
         */
        public List<Map.Entry<String, Integer>> breakTiesRandomly(List<Map.Entry<String, Integer>> tiedParties, Map<String, Integer> partyVotes) {
            Random random = new Random();
            List<Map.Entry<String, Double>> randomNumbers = new ArrayList<>();

            for (Map.Entry<String, Integer> entry : tiedParties) {
                double randomNumber = random.nextDouble();
                randomNumbers.add(Map.entry(entry.getKey(), randomNumber));
            }

            double targetNumber = random.nextDouble();

            randomNumbers.sort((a, b) -> {
                double diffA = Math.abs(a.getValue() - targetNumber);
                double diffB = Math.abs(b.getValue() - targetNumber);
                int result = Double.compare(diffA, diffB);
                if (result == 0) { // In case of a tie in the random number comparison, compare the total number of votes
                    return Integer.compare(partyVotes.get(b.getKey()), partyVotes.get(a.getKey()));

                }
                return result;
            });

            List<Map.Entry<String, Integer>> sortedTiedParties = new ArrayList<>();
            for (Map.Entry<String, Double> entry : randomNumbers) {
                sortedTiedParties.add(Map.entry(entry.getKey(), tiedParties.stream().filter(e -> e.getKey().equals(entry.getKey())).findFirst().get().getValue()));
            }

            return sortedTiedParties;
        }

        // Getters and setters for the instance variables
        /**
         * Returns the name of the election.
         *
         * @return the election name
         */
        public String getElectionName() {
            return electionName;
        }

        /**
         * Sets the name of the election.
         *
         * @param electionName the election name to set
         */
        public void setElectionName(String electionName) {
            this.electionName = electionName;
        }

        /**
         * Returns the list of party names.
         *
         * @return the party names
         */
        public List<String> getPartyNames() {
            return partyNames;
        }

        /**
         * Sets the list of party names.
         *
         * @param partyNames the party names to set
         */
        public void setPartyNames(List<String> partyNames) {
            this.partyNames = partyNames;
        }

        /**
         * Returns the number of seats.
         *
         * @return the number of seats
         */
        public int getNumberOfSeats() {
            return numberOfSeats;
        }

        /**
         * Sets the number of seats.
         *
         * @param numberOfSeats the number of seats to set
         */
        public void setNumberOfSeats(int numberOfSeats) {
            this.numberOfSeats = numberOfSeats;
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
         * @param numberOfBallots the number of ballots to set
         */
        public void setNumberOfBallots(int numberOfBallots) {
            this.numberOfBallots = numberOfBallots;
        }

        /**
         * Returns the list of ballots.
         *
         * @return the list of ballots
         */
        public List<String> getBallots() {
            return ballots;
        }

        /**
         * Sets the list of ballots.
         *
         * @param ballots the list of ballots to set
         */
        public void setBallots(List<String> ballots) {
            this.ballots = ballots;
        }

        /**
         * Returns the map of party votes.
         *
         * @return the map of party votes
         */
        public Map<String, Integer> getPartyVotes() {
            return partyVotes;
        }

        /**
         * Sets the map of party votes.
         *
         * @param partyVotes the map of party votes to set
         */
        public void setPartyVotes(Map<String, Integer> partyVotes) {
            this.partyVotes = partyVotes;
        }

        /**
         * Returns the map of election results (seat distribution).
         *
         * @return the map of election results
         */
        public Map<String, Integer> getResults() {
            return results;
        }

        /**
         * Sets the map of election results (seat distribution).
         *
         * @param results the map of election results to set
         */
        public void setResults(Map<String, Integer> results) {
            this.results = results;
        }
    }

}
