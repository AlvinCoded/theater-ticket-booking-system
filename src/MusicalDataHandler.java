import java.sql.*;
import java.util.*;
import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;


public class MusicalDataHandler {

    protected final List<Musical> musicals;
    protected static final String DB_URL = "jdbc:mysql://localhost:3306/musical_tickets";
    protected static final String USER = "root";
    protected static final String PASS = "";

    public MusicalDataHandler() {
        musicals = new ArrayList<>();
        try {
            loadMusicalData();
        } catch (Exception e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }

     private void loadMusicalData() {
         try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
             String query = "SELECT * FROM musicals";
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query);

             while (rs.next()) {
                 Musical musical = new Musical(
                     rs.getInt("id"),
                     rs.getString("name"),
                     rs.getString("run_time"),
                     rs.getString("categories"),
                     rs.getString("age_restriction"),
                     rs.getDouble("price"),
                     rs.getInt("available_tickets"),
                     rs.getString("available_days")
                 );
                 musicals.add(musical);
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }

     public Musical getMusicalByName(String name) {
         try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
             String query = "SELECT * FROM musicals WHERE name = ?";
             PreparedStatement pstmt = conn.prepareStatement(query);
             pstmt.setString(1, name);
             ResultSet rs = pstmt.executeQuery();

             if (rs.next()) {
                 return new Musical(
                     rs.getInt("id"),
                     rs.getString("name"),
                     rs.getString("run_time"),
                     rs.getString("categories"),
                     rs.getString("age_restriction"),
                     rs.getDouble("price"),
                     rs.getInt("available_tickets"),
                     rs.getString("available_days")
                 );
             }
         } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
         return null;
     }

     public void updateTicketCount(String musicalName, int newCount) {
         try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
             String query = "UPDATE musicals SET available_tickets = ? WHERE name = ?";
             PreparedStatement pstmt = conn.prepareStatement(query);
             pstmt.setInt(1, newCount);
             pstmt.setString(2, musicalName);
             pstmt.executeUpdate();

             // Update local cache
             for (Musical musical : musicals) {
                 if (musical.getName().equals(musicalName)) {
                     musical.setAvailableTickets(newCount);
                     break;
                 }
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }

     public int getCustomerId(String username) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String query = "SELECT id FROM customers WHERE username = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("id");
            } else {
                throw new SQLException("Customer not found: " + username);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }


     public void saveReceipt(String receipt, String filePath) {
         try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
             String query = "INSERT INTO receipts (receipt_text, created_at) VALUES (?, NOW())";
             PreparedStatement pstmt = conn.prepareStatement(query);
             pstmt.setString(1, receipt);
             pstmt.executeUpdate();
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }

     public List<Musical> getMusicals() {
         return musicals;
     }

    public class Musical {
        private int id;
        private String name;
        private String runTime;
        private String categories;
        private String ageRestriction;
        private double price;
        private int availableTickets;
        private String availableDays;

        // Constructor
        public Musical(int id, String name, String runTime, String categories, String ageRestriction, double price, 
                      int availableTickets, String availableDays) 
        {
            this.id = id;
            this.name = name;
            this.runTime = runTime;
            this.categories = categories;
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
   

    public class AuthenticationResult {
        private boolean authenticated;
        private String role;
        private String userType;

        public AuthenticationResult(boolean authenticated, String role, String userType) {
            this.authenticated = authenticated;
            this.role = role;
            this.userType = userType;
        }

        public boolean isAuthenticated() { return authenticated; }
        public String getRole() { return role; }
        public String getUserType() { return userType; }
    }

    public AuthenticationResult authenticateUser(String username, String password) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            // First try staff table
            String staffQuery = "SELECT role FROM staff WHERE username = ? AND password = ?";
            PreparedStatement staffStmt = conn.prepareStatement(staffQuery);
            staffStmt.setString(1, username);
            staffStmt.setString(2, password);
            ResultSet staffRs = staffStmt.executeQuery();

            if (staffRs.next()) {
                return new AuthenticationResult(true, staffRs.getString("role"), "STAFF");
            }

            // If not found in staff, try customers table
            String customerQuery = "SELECT * FROM customers WHERE username = ? AND password = ?";
            PreparedStatement custStmt = conn.prepareStatement(customerQuery);
            custStmt.setString(1, username);
            custStmt.setString(2, password);
            ResultSet custRs = custStmt.executeQuery();

            if (custRs.next()) {
                return new AuthenticationResult(true, "CUSTOMER", "CUSTOMER");
            }

            return new AuthenticationResult(false, null, null);
        } catch (SQLException e) {
            e.printStackTrace();
            return new AuthenticationResult(false, null, null);
        }
    }

    public boolean registerCustomer(String username, String password, String email, String phone) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String query = "INSERT INTO customers (username, password, email, phone_number) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, email);
            pstmt.setString(4, phone);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getUserType(String username) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            // Check staff table first
            String staffQuery = "SELECT 'STAFF' as type FROM staff WHERE username = ?";
            PreparedStatement staffStmt = conn.prepareStatement(staffQuery);
            staffStmt.setString(1, username);
            ResultSet staffRs = staffStmt.executeQuery();

            if (staffRs.next()) {
                return "STAFF";
            }

            // Check customers table
            String customerQuery = "SELECT 'CUSTOMER' as type FROM customers WHERE username = ?";
            PreparedStatement custStmt = conn.prepareStatement(customerQuery);
            custStmt.setString(1, username);
            ResultSet custRs = custStmt.executeQuery();

            if (custRs.next()) {
                return "CUSTOMER";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    
    public boolean createStaffMember(String username, String password, String role, String permissions) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String query = "INSERT INTO staff (username, password, role, permissions) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role);
            pstmt.setString(4, permissions);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<StaffMember> getAllStaff() {
        List<StaffMember> staffList = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String query = "SELECT * FROM staff";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                staffList.add(new StaffMember(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("role"),
                    rs.getString("permissions"),
                    rs.getTimestamp("created_at")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return staffList;
    }

    public boolean updateStaffPermissions(int staffId, String role, String permissions) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String query = "UPDATE staff SET role = ?, permissions = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, role);
            pstmt.setString(2, permissions);
            pstmt.setInt(3, staffId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static class StaffMember {
        private int id;
        private String username;
        private String role;
        private String permissions;
        private Timestamp createdAt;

        public StaffMember(int id, String username, String role, String permissions, Timestamp createdAt) {
            this.id = id;
            this.username = username;
            this.role = role;
            this.permissions = permissions;
            this.createdAt = createdAt;
        }

        // Getters
        public int getId() { return id; }
        public String getUsername() { return username; }
        public String getRole() { return role; }
        public String getPermissions() { return permissions; }
        public Timestamp getCreatedAt() { return createdAt; }
    }

    public boolean updateStaffMemberWithPassword(int staffId, String role, String permissions, String hashedPassword) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String query = "UPDATE staff SET role = ?, permissions = ?, password = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, role);
            pstmt.setString(2, permissions);
            pstmt.setString(3, hashedPassword);
            pstmt.setInt(4, staffId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateStaffMember(int staffId, String role, String permissions) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String query = "UPDATE staff SET role = ?, permissions = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, role);
            pstmt.setString(2, permissions);
            pstmt.setInt(3, staffId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteStaffMember(int staffId) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String query = "DELETE FROM staff WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, staffId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getAdminCount() {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String query = "SELECT COUNT(*) FROM staff WHERE role = 'ADMIN'";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public String getStaffRole(int staffId) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String query = "SELECT role FROM staff WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, staffId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("role");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getStaffPermissions(String username) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String query = "SELECT permissions FROM staff WHERE username = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("permissions");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }
    
    public List<CustomerInfo> getAllCustomers() {
        List<CustomerInfo> customers = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String query = "SELECT id, username, email, phone_number, created_at FROM customers";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                customers.add(new CustomerInfo(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("phone_number"),
                    rs.getTimestamp("created_at")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }

    public boolean updateCustomer(int customerId, String username, String password, String email, String phone) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            // If password is empty, update without changing password
            if (password.isEmpty()) {
                String query = "UPDATE customers SET username = ?, email = ?, phone_number = ? WHERE id = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setString(1, username);
                pstmt.setString(2, email);
                pstmt.setString(3, phone);
                pstmt.setInt(4, customerId);
                return pstmt.executeUpdate() > 0;
            } else {
                // Update with new password
                String query = "UPDATE customers SET username = ?, password = ?, email = ?, phone_number = ? WHERE id = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                pstmt.setString(3, email);
                pstmt.setString(4, phone);
                pstmt.setInt(5, customerId);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteCustomer(int customerId) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String query = "DELETE FROM customers WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, customerId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean addVenue(String name, int capacity) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String query = "INSERT INTO venues (name, total_capacity) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, name);
            pstmt.setInt(2, capacity);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateVenue(int venueId, String name, int capacity) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String query = "UPDATE venues SET name = ?, total_capacity = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, name);
            pstmt.setInt(2, capacity);
            pstmt.setInt(3, venueId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean addSection(int venueId, String name, int capacity, double basePrice) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String query = "INSERT INTO sections (venue_id, name, capacity, base_price) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, venueId);
            pstmt.setString(2, name);
            pstmt.setInt(3, capacity);
            pstmt.setDouble(4, basePrice);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean deleteSection(int sectionId) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String query = "DELETE FROM sections WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, sectionId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public List<VenueSection> getVenueSections(int venueId) {
        List<VenueSection> sections = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String query = "SELECT * FROM sections WHERE venue_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, venueId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                sections.add(new VenueSection(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("capacity"),
                    rs.getDouble("base_price")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sections;
    }

    public List<String> getBookedSeats(String musicalName, int venueId, int sectionId,
                                  LocalDate showDate, String showTime) {
        List<String> bookedSeats = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            // Format the time to match database storage format
            String formattedTime = showTime.substring(0, 5); // Gets only HH:mm part
            
            String query = "SELECT seat_number FROM booked_seats WHERE " +
                        "musical_id = (SELECT id FROM musicals WHERE name = ?) " +
                        "AND venue_id = ? " +
                        "AND section_id = ? " +
                        "AND show_date = ? " +
                        "AND show_time LIKE ?";
            
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, musicalName);
            pstmt.setInt(2, venueId);
            pstmt.setInt(3, sectionId);
            pstmt.setDate(4, java.sql.Date.valueOf(showDate));
            pstmt.setString(5, formattedTime + "%");
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                bookedSeats.add(rs.getString("seat_number"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookedSeats;
    }



    public List<VenueWithSections> getVenuesForMusical(String musicalName) {
        List<VenueWithSections> venues = new ArrayList<>();
        Map<Integer, VenueWithSections> venueMap = new HashMap<>();
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String query = "SELECT v.*, s.* FROM venues v " +
                          "JOIN musical_venues mv ON v.id = mv.venue_id " +
                          "JOIN musicals m ON mv.musical_id = m.id " +
                          "JOIN sections s ON v.id = s.venue_id " +
                          "WHERE m.name = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, musicalName);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int venueId = rs.getInt("v.id");
                try {
                    VenueWithSections venue = venueMap.computeIfAbsent(venueId,
                        id -> {
                            try {
                                return new VenueWithSections(
                                    id,
                                    rs.getString("v.name"),
                                    new ArrayList<>()
                                );
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        });

                    venue.addSection(new VenueSection(
                        rs.getInt("s.id"),
                        rs.getString("s.name"),
                        rs.getInt("s.capacity"),
                        rs.getDouble("s.base_price")
                    ));
                } catch (RuntimeException e) {
                    if (e.getCause() instanceof SQLException) {
                        e.printStackTrace();
                    }
                    throw e;
                }
            }
            venues.addAll(venueMap.values());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return venues;
    }

    public static class VenueSection {
        private final int id;
        private final String name;
        private final int capacity;
        private final double basePrice;

        public VenueSection(int id, String name, int capacity, double basePrice) {
            this.id = id;
            this.name = name;
            this.capacity = capacity;
            this.basePrice = basePrice;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public int getCapacity() { return capacity; }
        public double getBasePrice() { return basePrice; }
    }

    public class VenueWithSections {
        private final int id;
        private final String name;
        private final List<VenueSection> sections;

        public VenueWithSections(int id, String name, List<VenueSection> sections) {
            this.id = id;
            this.name = name;
            this.sections = sections;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public List<VenueSection> getSections() { return sections; }
        public void addSection(VenueSection section) {
            sections.add(section);
        }
    }

    public static class BookedSeat {
        private final String seatNumber;
        private final double price;
        private final int venueId;
        private final int sectionId;

        public BookedSeat(String seatNumber, double price, int venueId, int sectionId) {
            this.seatNumber = seatNumber;
            this.price = price;
            this.venueId = venueId;
            this.sectionId = sectionId;
        }

        public String getSeatNumber() { return seatNumber; }
        public double getPrice() { return price; }
        public int getVenueId() { return venueId; }
        public int getSectionId() { return sectionId; }
    }

    public static class CustomerInfo {
        private int id;
        private String username;
        private String email;
        private String phoneNumber;
        private Timestamp createdAt;

        public CustomerInfo(int id, String username, String email, String phoneNumber, Timestamp createdAt) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.phoneNumber = phoneNumber;
            this.createdAt = createdAt;
        }

        // Getters
        public int getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getPhoneNumber() { return phoneNumber; }
        public Timestamp getCreatedAt() { return createdAt; }
    }

    
}
