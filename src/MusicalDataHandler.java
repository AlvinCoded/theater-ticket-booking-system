import java.io.*;
import java.util.*;

public class MusicalDataHandler {

    List<Musical> musicals;  // List to hold musical data

    // Constructor to load musical data from the CSV file
    public MusicalDataHandler(String filePath) throws IOException {
        musicals = new ArrayList<>();
        loadMusicalData(filePath);
    }

    // Method to load musical data from a CSV file
    private void loadMusicalData(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        reader.readLine(); // Skip the header line

        // Reading each line of the CSV and parsing it
        while ((line = reader.readLine()) != null) {
            String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1); // This regex allows commas within quoted fields
            Musical musical = new Musical(
                    Integer.parseInt(data[0]),   // ID
                    data[1],                     // Name
                    data[2],                     // Run time
                    data[3],                     // Categories
                    data[4],                     // Venue
                    data[5],                     // Age restriction
                    Double.parseDouble(data[6]),  // Price
                    Integer.parseInt(data[7]),    // Available tickets
                    data[8].trim()               // Available days as string
            );
            musicals.add(musical);  // Add musical to the list
        }
        reader.close();
    }

    // Method to get the musical by name
    public Musical getMusicalByName(String name) {
        for (Musical musical : musicals) {
            if (musical.getName().equalsIgnoreCase(name)) {
                return musical;
            }
        }
        return null;  // Return null if not found
    }

    // Method to save the receipt to a text file
    public void saveReceipt(String receipt, String filePath) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
        writer.write(receipt);
        writer.close();
    }

    public class Musical {
        private int id;
        private String name;
        private String runTime;
        private String categories;
        private String venue;
        private String ageRestriction;
        private double price;
        private int availableTickets;
        private String availableDays;

        // Constructor
        public Musical(int id, String name, String runTime, String categories, 
                      String venue, String ageRestriction, double price, 
                      int availableTickets, String availableDays) 
        {
            this.id = id;
            this.name = name;
            this.runTime = runTime;
            this.categories = categories;
            this.venue = venue;
            this.ageRestriction = ageRestriction;
            this.price = price;
            this.availableTickets = availableTickets;
            this.availableDays = availableDays;
        }

        // Getters for the attributes
        public int getId() {
            return id;
        }
        public String getName() {
            return name;
        }
        public String getRunTime() {
            return runTime;
        }
        public String getCategories() {
            return categories;
        }
        public String getVenue() {
            return venue;
        }   
        public String getAgeRestriction() {
            return ageRestriction;
        }
        public double getPrice() {
            return price;
        }
        public int getAvailableTickets() {
            return availableTickets;
        }
        public void setAvailableTickets(int availableTickets) {
            this.availableTickets = availableTickets;
        }
        public String getAvailableDays() {
            return availableDays;
        }
    }
}
