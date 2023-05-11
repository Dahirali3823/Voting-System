import java.util.*;
import java.io.*;
/*
A nested public static class called "ElectionData" is contained within the Java class "CPLClass," which is named.
The name of the election, a list of the parties that participated, the number of seats available,
the number of ballots cast, and the actual ballots themselves are just a few of the election-related data that can be stored in this class's private fields.
Two constructors are provided by the class, one of which accepts a number of arguments and sets the initial values for the fields, 
and the other of which accepts a file path and initializes the fields by reading data from a file.
Additionally, the class offers a number of techniques for handling election data.
One method counts the votes for each party based on the ballots cast, while another determines how many seats are allocated to each party based on the percentage of votes they received.
The class offers a method for saving the outcomes to a file as well.
To break ties between parties that have the same number of votes, the class also includes a private helper method. 
 */
public class CPLClass {
    public static class ElectionData {
        private String electionName;
        private List<String> partyNames;
        private int numberOfSeats;
        private int numberOfBallots;
        private List<String> ballots;
        private Map<String, Integer> partyVotes;
        private Map<String, Integer> results;

        public ElectionData(String electionName, List<String> partyNames, int numberOfSeats, int numberOfBallots,
                List<String> ballots) {
            this.electionName = electionName;
            this.partyNames = partyNames;
            this.numberOfSeats = numberOfSeats;
            this.numberOfBallots = numberOfBallots;
            this.ballots = ballots;
            this.partyVotes = new HashMap<>();
            this.results = new HashMap<>();

        }

        public ElectionData(String file) throws IOException {
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

        }

        public void processElection() {
            int totalVotes = partyVotes.values().stream().mapToInt(Integer::intValue).sum();
            

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
                int remainder = partyVotes.get(party) % (totalVotes / numberOfSeats);

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

        private List<Map.Entry<String, Integer>> breakTiesRandomly(List<Map.Entry<String, Integer>> tiedParties,
                Map<String, Integer> partyVotes) {
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
                if (result == 0) { // In case of a tie in the random number comparison, compare the total number of
                                   // votes
                    return Integer.compare(partyVotes.get(b.getKey()), partyVotes.get(a.getKey()));

                }
                return result;
            });

            List<Map.Entry<String, Integer>> sortedTiedParties = new ArrayList<>();
            for (Map.Entry<String, Double> entry : randomNumbers) {
                sortedTiedParties.add(Map.entry(entry.getKey(), tiedParties.stream()
                        .filter(e -> e.getKey().equals(entry.getKey())).findFirst().get().getValue()));
            }

            return sortedTiedParties;
        }

        public String getElectionName() {
            return electionName;
        }

        public void setElectionName(String electionName) {
            this.electionName = electionName;
        }

        public List<String> getPartyNames() {
            return partyNames;
        }

        public void setPartyNames(List<String> partyNames) {
            this.partyNames = partyNames;
        }

        public int getNumberOfSeats() {
            return numberOfSeats;
        }

        public void setNumberOfSeats(int numberOfSeats) {
            this.numberOfSeats = numberOfSeats;
        }

        public int getNumberOfBallots() {
            return numberOfBallots;
        }

        public void setNumberOfBallots(int numberOfBallots) {
            this.numberOfBallots = numberOfBallots;
        }

        public List<String> getBallots() {
            return ballots;
        }

        public void setBallots(List<String> ballots) {
            this.ballots = ballots;
        }

        public Map<String, Integer> getPartyVotes() {
            return partyVotes;
        }

        public void setPartyVotes(Map<String, Integer> partyVotes) {
            this.partyVotes = partyVotes;
        }

        public Map<String, Integer> getResults() {
            return results;
        }

        public void setResults(Map<String, Integer> results) {
            this.results = results;
        }
    }

}
