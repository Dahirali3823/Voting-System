/*
ElectionProcessor Class:
Processing elections for the two types of "CPL" and "IR" elections is the responsibility of this class, ElectionProcessor.
The required data is read from a CSV file with the default name input.csv, validated, and then the user is prompted for the information before the election is processed.
Information about the election is stored in a number of private instance variables that belong to the class.
These elements are the following initilizations.

CPLClass Class:
A nested public static class called "ElectionData" is contained within the Java class "CPLClass," which is named.
The name of the election, a list of the parties that participated, the number of seats available,
the number of ballots cast, and the actual ballots themselves are just a few of the election-related data that can be stored in this class's private fields.
Two constructors are provided by the class, one of which accepts a number of arguments and sets the initial values for the fields, 
and the other of which accepts a file path and initializes the fields by reading data from a file.
Additionally, the class offers a number of techniques for handling election data.
One method counts the votes for each party based on the ballots cast, while another determines how many seats are allocated to each party based on the percentage of votes they received.
The class offers a method for saving the outcomes to a file as well.
To break ties between parties that have the same number of votes, the class also includes a private helper method.

InstantRunoff Class:
For a specific election, an instant-runoff voting system is implemented by the Java class InstantRunoff.
All the information required to conduct the election is contained in a nested class called ElectionData.
Two constructors are available for the ElectionData class: one that accepts all the data required to conduct the election and another that accepts the path to a file containing the election data.
The instant-runoff voting algorithm is run by the runIR() method, which then returns the election's victor.
The produceAuditFile() method generates a text file that records the election's specifics, including the number of votes cast for each candidate.
In the event of a tie, the fairCoinToss() helper method conducts a fair coin toss between the candidates.
*/