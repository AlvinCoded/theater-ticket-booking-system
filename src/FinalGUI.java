import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List; 
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FinalGUI {

    private static JPanel mainPanel;
    private static CardLayout cardLayout;
    private static MusicalDataHandler dataHandler;
    private static String currentUser;
    private static String userRole;
    private static JTextArea musicalTextArea;
    private static JTextArea seatInfoTextArea;
    private static JComboBox<String> categoryDropdown;
     private static JPanel musicalListPanel;
    private static JPanel welcomePanel;
    private static JPanel contentPanel;
    private static boolean isShowingSchedule = false;

    public static void main(String[] args) {
        try {
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Initialize data handler
            dataHandler = new MusicalDataHandler();
            
            // Create the main frame
            JFrame frame = new JFrame("Musical Theater Tickets");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 400);

            // Create a CardLayout
            cardLayout = new CardLayout();
            mainPanel = new JPanel(cardLayout);

            // Create and add the splash screen to the main panel
            mainPanel.add(createSplashScreen(), "SplashScreen");

            // Add panels (views) to the main panel
            mainPanel.add(createMusicalListPanel(), "MusicalListPanel");
            mainPanel.add(createBookingPanel(), "BookingPanel");

            // Add the main panel to the frame
            frame.add(mainPanel);
            frame.setVisible(true);

            // Show the initial splash screen
            cardLayout.show(mainPanel, "SplashScreen");

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database driver not found!");
            System.exit(1);
        }

        
    }

    
    // Method to create the initial splash screen with background image
    private static JPanel createSplashScreen() {
        JPanel splashPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Create login panel
        JPanel loginPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        loginPanel.setBorder(BorderFactory.createTitledBorder("Login"));

        JTextField usernameField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(15);
        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        loginPanel.add(new JLabel("Username:"));
        loginPanel.add(usernameField);
        loginPanel.add(new JLabel("Password:"));
        loginPanel.add(passwordField);
        loginPanel.add(loginButton);
        loginPanel.add(registerButton);

        // Login button action
        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            MusicalDataHandler.AuthenticationResult authResult = dataHandler.authenticateUser(username, password);

            if (authResult.isAuthenticated()) {
                currentUser = username; // Set the current user
                userRole = authResult.getRole();
                String userType = authResult.getUserType();

                if ("STAFF".equals(userType)) {
                    if ("ADMIN".equals(userRole)) {
                        showAdminDashboard();
                    } else {
                        // Show staff dashboard with limited permissions
                        showAdminDashboard();
                    }
                } else {
                    // Customer view
                    cardLayout.show(mainPanel, "MusicalListPanel");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Invalid credentials!");
            }
        });


        registerButton.addActionListener(e -> {
            showRegistrationDialog();
        });

        gbc.gridx = 0;
        gbc.gridy = 0;
        splashPanel.add(loginPanel, gbc);

        return splashPanel;
    }

    private static void showAdminDashboard() {
        // Create new frame for admin dashboard
        JFrame adminFrame = new JFrame("Admin Dashboard");
        adminFrame.setSize(1200, 700);
        adminFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create main split pane
        JSplitPane splitPane = new JSplitPane();
        splitPane.setDividerLocation(200);

        // Create side menu
        JPanel sideMenu = new JPanel();
        sideMenu.setLayout(new BoxLayout(sideMenu, BoxLayout.Y_AXIS));
        sideMenu.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create menu buttons
        JButton dashboardBtn = new JButton("Dashboard");
        JButton manageMusicalBtn = new JButton("Manage Musicals");
        JButton addMusicalBtn = new JButton("Add Musical");
        JButton editMusicalBtn = new JButton("Edit Musical");
        JButton deleteMusicalBtn = new JButton("Delete Musical");
        JButton viewBookingsBtn = new JButton("View Bookings");
        JButton customerManagementBtn = new JButton("Customer Management");
        JButton staffManagementBtn = new JButton("Staff Management");
        JButton venueManagementBtn = new JButton("Venue Management");
        JButton logoutBtn = new JButton("Logout");

        // Style buttons
        Dimension buttonSize = new Dimension(180, 40);    
        Component[] buttons = {dashboardBtn, manageMusicalBtn, addMusicalBtn, 
                             editMusicalBtn, deleteMusicalBtn, venueManagementBtn, 
                             viewBookingsBtn, customerManagementBtn, staffManagementBtn, logoutBtn};

        for (Component btn : buttons) {
            btn.setMaximumSize(buttonSize);
            btn.setPreferredSize(buttonSize);
            sideMenu.add(btn);
            sideMenu.add(Box.createVerticalStrut(10));
        }
        // Create content panel with CardLayout
        JPanel contentPanel = new JPanel(new CardLayout());
        CardLayout contentLayout = (CardLayout) contentPanel.getLayout();

        // Add panels to content
        contentPanel.add(new AdminDashboard(dataHandler, dataHandler.getStaffPermissions(currentUser)), "Dashboard");
        contentPanel.add(createAdminMusicalListPanel(), "ManageMusicals");
        contentPanel.add(new CustomerManagementPanel(dataHandler), "CustomerManagement");
        contentPanel.add(new StaffManagementPanel(dataHandler), "StaffManagement");
        contentPanel.add(new VenueManagementPanel(dataHandler), "VenueManagement");
        contentPanel.add(new BookingHistoryPanel(dataHandler), "ViewBookings");

        // Add action listeners
        dashboardBtn.addActionListener(e -> contentLayout.show(contentPanel, "Dashboard"));
        manageMusicalBtn.addActionListener(e -> contentLayout.show(contentPanel, "ManageMusicals"));
        customerManagementBtn.addActionListener(e -> contentLayout.show(contentPanel, "CustomerManagement"));
        staffManagementBtn.addActionListener(e -> contentLayout.show(contentPanel, "StaffManagement"));
        venueManagementBtn.addActionListener(e -> contentLayout.show(contentPanel, "VenueManagement"));
        viewBookingsBtn.addActionListener(e -> contentLayout.show(contentPanel, "ViewBookings"));
        addMusicalBtn.addActionListener(e -> showAddMusicalDialog());
        editMusicalBtn.addActionListener(e -> showEditMusicalDialog());
        deleteMusicalBtn.addActionListener(e -> showDeleteMusicalDialog());
        logoutBtn.addActionListener(e -> {
            adminFrame.dispose();
            JFrame mainFrame = (JFrame) SwingUtilities.getWindowAncestor(mainPanel);
            mainFrame.setVisible(true);
            cardLayout.show(mainPanel, "SplashScreen");
        });

        // Add components to split pane
        splitPane.setLeftComponent(sideMenu);
        splitPane.setRightComponent(contentPanel);

        // Add split pane to frame
        adminFrame.add(splitPane);
        adminFrame.setLocationRelativeTo(null);
        adminFrame.setVisible(true);

        // Hide the main application window
        JFrame mainFrame = (JFrame) SwingUtilities.getWindowAncestor(mainPanel);
        mainFrame.setVisible(false);
    }
    
    private static void showRegistrationDialog() {
        JDialog dialog = new JDialog((Frame)null, "Register New User", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField usernameField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(15);
        JPasswordField confirmPasswordField = new JPasswordField(15);
        JTextField emailField = new JTextField(15);
        JTextField phoneField = new JTextField(15);

        // Add tooltips
        emailField.setToolTipText("Enter a valid email address");
        phoneField.setToolTipText("Enter your phone number");

        // Add components with labels
        addFormField(dialog, gbc, "Username:", usernameField, 0);
        addFormField(dialog, gbc, "Password:", passwordField, 1);
        addFormField(dialog, gbc, "Confirm Password:", confirmPasswordField, 2);
        addFormField(dialog, gbc, "Email:", emailField, 3);
        addFormField(dialog, gbc, "Phone:", phoneField, 4);

        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String confirm = new String(confirmPasswordField.getPassword());
            String email = emailField.getText();
            String phone = phoneField.getText();

            // Validate input
            if (username.isEmpty() || password.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields must be filled.");
                return;
            }

            if (!password.equals(confirm)) {
                JOptionPane.showMessageDialog(dialog, "Passwords do not match!");
                return;
            }

            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid email address.");
                return;
            }

            if (!phone.matches("^\\d{10}$")) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid 10-digit phone number.");
                return;
            }

            if (dataHandler.registerCustomer(username, password, email, phone)) {
                JOptionPane.showMessageDialog(dialog, "Registration successful!");
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Registration failed. Username may already exist.");
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        dialog.add(registerButton, gbc);

        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    private static void addFormField(JDialog dialog, GridBagConstraints gbc, 
                                   String label, JComponent field, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        dialog.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        dialog.add(field, gbc);
    }


    private static void showAddMusicalDialog() {
        JDialog dialog = new JDialog((Frame)null, "Add New Musical", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Create form fields with tooltips
        JTextField nameField = createFieldWithTooltip("Enter the musical's name", 20);
        JTextField runTimeField = createFieldWithTooltip("Enter run time (e.g., 2h 30min)", 20);
        JTextField categoriesField = createFieldWithTooltip("Enter categories separated by commas", 20);
        JTextField ageRestrictionField = createFieldWithTooltip("Enter age restriction (e.g., 12+, All ages)", 20);
        JTextField priceField = createFieldWithTooltip("Enter ticket price", 20);
        JTextField ticketsField = createFieldWithTooltip("Enter number of available tickets", 20);
        JTextField daysField = createFieldWithTooltip("Enter available days separated by commas", 20);

        // Create venue selection list
        DefaultListModel<String> venueListModel = new DefaultListModel<>();
        JList<String> venueList = new JList<>(venueListModel);
        venueList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // Populate venue list
        try (Connection conn = DriverManager.getConnection(
                dataHandler.DB_URL, dataHandler.USER, dataHandler.PASS)) {
            String query = "SELECT id, name, total_capacity FROM venues";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String venueDisplay = String.format("%s (Capacity: %d)", 
                    rs.getString("name"), rs.getInt("total_capacity"));
                venueListModel.addElement(venueDisplay);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JScrollPane venueScrollPane = new JScrollPane(venueList);
        venueScrollPane.setPreferredSize(new Dimension(300, 100));

        // Add components to dialog
        addLabelAndField(dialog, gbc, "Name:", nameField, 0);
        addLabelAndField(dialog, gbc, "Run Time:", runTimeField, 1);
        addLabelAndField(dialog, gbc, "Categories:", categoriesField, 2);
        addLabelAndField(dialog, gbc, "Age Restriction:", ageRestrictionField, 3);
        addLabelAndField(dialog, gbc, "Price:", priceField, 4);
        addLabelAndField(dialog, gbc, "Available Tickets:", ticketsField, 5);
        addLabelAndField(dialog, gbc, "Available Days:", daysField, 6);

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        dialog.add(new JLabel("Select Venues:"), gbc);

        gbc.gridy = 8;
        dialog.add(venueScrollPane, gbc);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            // Validate selected venues
            List<String> selectedVenues = venueList.getSelectedValuesList();
            Set<String> uniqueVenues = new HashSet<>(selectedVenues);

            if (selectedVenues.size() != uniqueVenues.size()) {
                JOptionPane.showMessageDialog(dialog, "Each venue can only be selected once!");
                return;
            }

             if (validateMusicalInput(nameField, runTimeField, categoriesField, ageRestrictionField, priceField, ticketsField, daysField, venueList)) 
             {
                // Save musical and venue associations
                saveNewMusicalWithVenues(nameField.getText(), runTimeField.getText(), 
                    categoriesField.getText(), ageRestrictionField.getText(),
                    Double.parseDouble(priceField.getText()),
                    Integer.parseInt(ticketsField.getText()), 
                    daysField.getText(), selectedVenues);
                dialog.dispose();
            }
        });

        gbc.gridy = 9;
        dialog.add(saveButton, gbc);

        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }
    
    
    
    private static void saveNewMusicalWithVenues(String name, String runTime,
        String categories, String ageRestriction, double price,
        int availableTickets, String availableDays, List<String> selectedVenues) {

        try (Connection conn = DriverManager.getConnection(
                dataHandler.DB_URL, dataHandler.USER, dataHandler.PASS)) {

            conn.setAutoCommit(false);
            try {
                // First verify all venue capacities again
                for (String venueStr : selectedVenues) {
                    int capacity = extractVenueCapacity(venueStr);
                    if (availableTickets > capacity) {
                        throw new SQLException("Venue capacity exceeded for " + 
                            extractVenueName(venueStr));
                    }
                }

                // Insert musical
                String musicalQuery = "INSERT INTO musicals (name, run_time, categories, " +
                    "age_restriction, price, available_tickets, available_days) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

                PreparedStatement pstmt = conn.prepareStatement(musicalQuery,
                    Statement.RETURN_GENERATED_KEYS);

                pstmt.setString(1, name.trim());
                pstmt.setString(2, runTime.trim());
                pstmt.setString(3, categories.trim());
                pstmt.setString(4, ageRestriction.trim());
                pstmt.setDouble(5, price);
                pstmt.setInt(6, availableTickets);
                pstmt.setString(7, availableDays.trim());

                pstmt.executeUpdate();

                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int musicalId = rs.getInt(1);

                    // Insert venue associations
                    for (String venueStr : selectedVenues) {
                        String venueName = extractVenueName(venueStr);
                        String venueQuery = "INSERT INTO musical_venues (musical_id, venue_id) " +
                            "SELECT ?, id FROM venues WHERE name = ?";
                        PreparedStatement venueStmt = conn.prepareStatement(venueQuery);
                        venueStmt.setInt(1, musicalId);
                        venueStmt.setString(2, venueName);
                        venueStmt.executeUpdate();
                    }

                    conn.commit();
                    JOptionPane.showMessageDialog(null, "Musical added successfully!");
                }
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error adding musical: " + ex.getMessage());
        }
    }


    private static void updateMusicalWithVenues(String oldName, String name, String runTime,
        String categories, String ageRestriction, double price, 
        int availableTickets, String availableDays, List<String> selectedVenues) {

        try (Connection conn = DriverManager.getConnection(
                dataHandler.DB_URL, dataHandler.USER, dataHandler.PASS)) {
            conn.setAutoCommit(false);
            try {
                // Update musical
                String updateQuery = "UPDATE musicals SET name=?, run_time=?, categories=?, " +
                    "age_restriction=?, price=?, available_tickets=?, available_days=? WHERE name=?";
                PreparedStatement pstmt = conn.prepareStatement(updateQuery);
                pstmt.setString(1, name);
                pstmt.setString(2, runTime);
                pstmt.setString(3, categories);
                pstmt.setString(4, ageRestriction);
                pstmt.setDouble(5, price);
                pstmt.setInt(6, availableTickets);
                pstmt.setString(7, availableDays);
                pstmt.setString(8, oldName);
                pstmt.executeUpdate();

                // Get musical ID
                String idQuery = "SELECT id FROM musicals WHERE name = ?";
                PreparedStatement idStmt = conn.prepareStatement(idQuery);
                idStmt.setString(1, name);
                ResultSet rs = idStmt.executeQuery();

                if (rs.next()) {
                    int musicalId = rs.getInt("id");

                    // Delete existing venue associations
                    String deleteVenuesQuery = "DELETE FROM musical_venues WHERE musical_id = ?";
                    PreparedStatement deleteStmt = conn.prepareStatement(deleteVenuesQuery);
                    deleteStmt.setInt(1, musicalId);
                    deleteStmt.executeUpdate();

                    // Insert new venue associations
                    for (String venueStr : selectedVenues) {
                        String venueName = venueStr.substring(0, venueStr.indexOf(" ("));
                        String insertVenueQuery = "INSERT INTO musical_venues (musical_id, venue_id) " +
                            "SELECT ?, id FROM venues WHERE name = ?";
                        PreparedStatement venueStmt = conn.prepareStatement(insertVenueQuery);
                        venueStmt.setInt(1, musicalId);
                        venueStmt.setString(2, venueName);
                        venueStmt.executeUpdate();
                    }
                }

                conn.commit();
                JOptionPane.showMessageDialog(null, "Musical updated successfully!");
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error updating musical: " + ex.getMessage());
        }
    }


    private static JTextField createFieldWithTooltip(String tooltip, int columns) {
        JTextField field = new JTextField(columns);
        field.setToolTipText(tooltip);
        return field;
    }

    private static void addLabelAndField(JDialog dialog, GridBagConstraints gbc, 
                                         String labelText, JTextField field, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        dialog.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        dialog.add(field, gbc);
    }

    private static boolean validateMusicalInput(JTextField nameField, JTextField runTimeField, JTextField categoriesField, JTextField ageRestrictionField, JTextField priceField, JTextField ticketsField, JTextField daysField, JList<String> venueList) {

        // Check if all fields are filled
        if (nameField.getText().trim().isEmpty() || runTimeField.getText().trim().isEmpty() ||
            categoriesField.getText().trim().isEmpty() || ageRestrictionField.getText().trim().isEmpty() ||
            priceField.getText().trim().isEmpty() || ticketsField.getText().trim().isEmpty() ||
            daysField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "All fields must be filled.");
            return false;
        }

        // Validate price
        try {
            Double.valueOf(priceField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Price must be a valid number.");
            return false;
        }

        // Validate available tickets
        int availableTickets;
        try {
            availableTickets = Integer.parseInt(ticketsField.getText().trim());
            if (availableTickets <= 0) {
                JOptionPane.showMessageDialog(null, "Available tickets must be greater than 0.");
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Available tickets must be a valid integer.");
            return false;
        }

        // Validate venue selection and capacity
        List<String> selectedVenues = venueList.getSelectedValuesList();
        if (selectedVenues.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please select at least one venue.");
            return false;
        }

        // Check venue capacity
        for (String venueStr : selectedVenues) {
            int capacity = extractVenueCapacity(venueStr);
            if (availableTickets > capacity) {
                JOptionPane.showMessageDialog(null, 
                    "Available tickets (" + availableTickets + ") exceed venue capacity (" + 
                    capacity + ") for venue: " + extractVenueName(venueStr));
                return false;
            }
        }

        // Validate available days
        String[] days = daysField.getText().trim().split(",");
        Set<String> validDays = new HashSet<>(Arrays.asList(
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
        ));

        for (String day : days) {
            String trimmedDay = day.trim();
            if (!validDays.contains(trimmedDay)) {
                JOptionPane.showMessageDialog(null, 
                    "Invalid day format. Please use full day names separated by commas.");
                return false;
            }
        }

        return true;
    }

    private static int extractVenueCapacity(String venueStr) {
        int startIndex = venueStr.indexOf("Capacity: ") + 10;
        int endIndex = venueStr.indexOf(")");
        return Integer.parseInt(venueStr.substring(startIndex, endIndex));
    }

    private static String extractVenueName(String venueStr) {
        return venueStr.substring(0, venueStr.indexOf(" ("));
    }

    
    private static void showEditMusicalDialog() {
        JDialog selectionDialog = new JDialog((Frame)null, "Select Musical to Edit", true);
        selectionDialog.setLayout(new BorderLayout(10, 10));
        selectionDialog.setSize(400, 300);

        // Create musical selection list
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> musicalList = new JList<>(listModel);
        musicalList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Populate list with musical names
        try (Connection conn = DriverManager.getConnection(MusicalDataHandler.DB_URL, MusicalDataHandler.USER, MusicalDataHandler.PASS)) {
            String query = "SELECT name FROM musicals";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                listModel.addElement(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JScrollPane scrollPane = new JScrollPane(musicalList);
        selectionDialog.add(scrollPane, BorderLayout.CENTER);

        JButton editButton = new JButton("Edit Selected");
        editButton.addActionListener(e -> {
            String selectedMusical = musicalList.getSelectedValue();
            if (selectedMusical != null) {
                selectionDialog.dispose();
                showEditForm(selectedMusical);
            } else {
                JOptionPane.showMessageDialog(selectionDialog, "Please select a musical to edit.");
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(editButton);
        selectionDialog.add(buttonPanel, BorderLayout.SOUTH);

        selectionDialog.setLocationRelativeTo(null);
        selectionDialog.setVisible(true);
    }

    private static void showEditForm(String musicalName) {
        JDialog editDialog = new JDialog((Frame)null, "Edit Musical: " + musicalName, true);
        editDialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Create form fields with tooltips
        JTextField nameField = createFieldWithTooltip("Enter the musical's name", 20);
        JTextField runTimeField = createFieldWithTooltip("Enter run time (e.g., 2h 30min)", 20);
        JTextField categoriesField = createFieldWithTooltip("Enter categories separated by commas", 20);
        JTextField ageRestrictionField = createFieldWithTooltip("Enter age restriction (e.g., 12+, All ages)", 20);
        JTextField priceField = createFieldWithTooltip("Enter ticket price", 20);
        JTextField ticketsField = createFieldWithTooltip("Enter number of available tickets", 20);
        JTextField daysField = createFieldWithTooltip("Enter available days separated by commas", 20);

        // Create venue selection list
        DefaultListModel<String> venueListModel = new DefaultListModel<>();
        JList<String> venueList = new JList<>(venueListModel);
        venueList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // Load current musical data and populate venue list
        try (Connection conn = DriverManager.getConnection(dataHandler.DB_URL, dataHandler.USER, dataHandler.PASS)) {
            // Load musical data
            String musicalQuery = "SELECT * FROM musicals WHERE name = ?";
            PreparedStatement pstmt = conn.prepareStatement(musicalQuery);
            pstmt.setString(1, musicalName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                nameField.setText(rs.getString("name"));
                runTimeField.setText(rs.getString("run_time"));
                categoriesField.setText(rs.getString("categories"));
                ageRestrictionField.setText(rs.getString("age_restriction"));
                priceField.setText(String.valueOf(rs.getDouble("price")));
                ticketsField.setText(String.valueOf(rs.getInt("available_tickets")));
                daysField.setText(rs.getString("available_days"));
            }

            // Load all venues with their capacities
            String venueQuery = "SELECT id, name, total_capacity FROM venues";
            Statement venueStmt = conn.createStatement();
            ResultSet venueRs = venueStmt.executeQuery(venueQuery);

            while (venueRs.next()) {
                String venueDisplay = String.format("%s (Capacity: %d)", 
                    venueRs.getString("name"), venueRs.getInt("total_capacity"));
                venueListModel.addElement(venueDisplay);
            }

            // Load and select current venues for this musical
            String selectedVenuesQuery = "SELECT v.name FROM venues v " +
                "JOIN musical_venues mv ON v.id = mv.venue_id " +
                "WHERE mv.musical_id = ?";
            PreparedStatement venuesPstmt = conn.prepareStatement(selectedVenuesQuery);
            venuesPstmt.setInt(1, rs.getInt("id"));
            ResultSet selectedVenuesRs = venuesPstmt.executeQuery();

            List<Integer> selectedIndices = new ArrayList<>();
            while (selectedVenuesRs.next()) {
                String venueName = selectedVenuesRs.getString("name");
                for (int i = 0; i < venueListModel.size(); i++) {
                    if (venueListModel.get(i).startsWith(venueName)) {
                        selectedIndices.add(i);
                    }
                }
            }
            venueList.setSelectedIndices(selectedIndices.stream().mapToInt(i -> i).toArray());

        } catch (SQLException e) {
            e.printStackTrace();
        }

        JScrollPane venueScrollPane = new JScrollPane(venueList);
        venueScrollPane.setPreferredSize(new Dimension(300, 100));

        // Add components to dialog
        addLabelAndField(editDialog, gbc, "Name:", nameField, 0);
        addLabelAndField(editDialog, gbc, "Run Time:", runTimeField, 1);
        addLabelAndField(editDialog, gbc, "Categories:", categoriesField, 2);
        addLabelAndField(editDialog, gbc, "Age Restriction:", ageRestrictionField, 3);
        addLabelAndField(editDialog, gbc, "Price:", priceField, 4);
        addLabelAndField(editDialog, gbc, "Available Tickets:", ticketsField, 5);
        addLabelAndField(editDialog, gbc, "Available Days:", daysField, 6);

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        editDialog.add(new JLabel("Select Venues:"), gbc);

        gbc.gridy = 8;
        editDialog.add(venueScrollPane, gbc);

        JButton updateButton = new JButton("Update");
        updateButton.addActionListener(e -> {
            List<String> selectedVenues = venueList.getSelectedValuesList();
            Set<String> uniqueVenues = new HashSet<>(selectedVenues);

            if (selectedVenues.size() != uniqueVenues.size()) {
                JOptionPane.showMessageDialog(editDialog, "Each venue can only be selected once!");
                return;
            }

            if (validateMusicalInput(nameField, runTimeField, categoriesField, ageRestrictionField, priceField, ticketsField, daysField, venueList)) 
            {
                updateMusicalWithVenues(musicalName, nameField.getText(), runTimeField.getText(),
                    categoriesField.getText(), ageRestrictionField.getText(),
                    Double.parseDouble(priceField.getText()),
                    Integer.parseInt(ticketsField.getText()),
                    daysField.getText(), selectedVenues);
                editDialog.dispose();
            }
        });

        gbc.gridy = 9;
        editDialog.add(updateButton, gbc);

        editDialog.pack();
        editDialog.setLocationRelativeTo(null);
        editDialog.setVisible(true);
    }

    private static void showDeleteMusicalDialog() {
        JDialog deleteDialog = new JDialog((Frame)null, "Delete Musical", true);
        deleteDialog.setLayout(new BorderLayout(10, 10));
        deleteDialog.setSize(400, 300);

        // Create musical selection list
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> musicalList = new JList<>(listModel);
        musicalList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Populate list with musical names
        try (Connection conn = DriverManager.getConnection(MusicalDataHandler.DB_URL, MusicalDataHandler.USER, MusicalDataHandler.PASS)) {
            String query = "SELECT name FROM musicals";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                listModel.addElement(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JScrollPane scrollPane = new JScrollPane(musicalList);
        deleteDialog.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton deleteButton = new JButton("Delete Selected");
        deleteButton.setBackground(new Color(255, 69, 69));
        deleteButton.setForeground(Color.WHITE);

        deleteButton.addActionListener(e -> {
            String selectedMusical = musicalList.getSelectedValue();
            if (selectedMusical != null) {
                int confirm = JOptionPane.showConfirmDialog(
                    deleteDialog,
                    "Are you sure you want to delete '" + selectedMusical + "'?\nThis action cannot be undone.",
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    try (Connection conn = DriverManager.getConnection(MusicalDataHandler.DB_URL, MusicalDataHandler.USER, MusicalDataHandler.PASS)) {
                        String deleteQuery = "DELETE FROM musicals WHERE name = ?";
                        PreparedStatement pstmt = conn.prepareStatement(deleteQuery);
                        pstmt.setString(1, selectedMusical);

                        int result = pstmt.executeUpdate();
                        if (result > 0) {
                            listModel.removeElement(selectedMusical);
                            JOptionPane.showMessageDialog(deleteDialog, "Musical deleted successfully!");
                        } else {
                            JOptionPane.showMessageDialog(deleteDialog, "Failed to delete musical.");
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(deleteDialog, "Error deleting musical: " + ex.getMessage());
                    }
                }
            } else {
                JOptionPane.showMessageDialog(deleteDialog, "Please select a musical to delete.");
            }
        });

        buttonPanel.add(deleteButton);
        deleteDialog.add(buttonPanel, BorderLayout.SOUTH);

        deleteDialog.setLocationRelativeTo(null);
        deleteDialog.setVisible(true);
    }

    
    // Method to create the Musical List Panel with Category Filter
    private static JPanel createMusicalListPanel() {
        JPanel musicalListPanel = new JPanel(new BorderLayout(10, 10));
        musicalListPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
          
        // Add welcome message at the top
        JLabel welcomeLabel = new JLabel();
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 20, 0));
        
        // Update welcome message whenever the panel becomes visible
        musicalListPanel.addAncestorListener(new javax.swing.event.AncestorListener() {
            @Override
            public void ancestorAdded(javax.swing.event.AncestorEvent event) {
                String welcomeText = getWelcomeMessage();
                if (currentUser != null) {
                    welcomeText += ", " + currentUser + "!";
                } else {
                    welcomeText += "!";
                }
                welcomeLabel.setText(welcomeText);
            }

            @Override
            public void ancestorRemoved(javax.swing.event.AncestorEvent event) {}

            @Override
            public void ancestorMoved(javax.swing.event.AncestorEvent event) {}
        });

        // Panel for buttons at the top
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 4, 10, 10));

        // Create buttons
        JButton homeButton = new JButton("Home");
        JButton showScheduleButton = new JButton("Show Schedule");
        JButton bookTicketsButton = new JButton("Book Tickets");
        JButton logoutButton = new JButton("Logout");

        // Add buttons to panel
        buttonPanel.add(homeButton);
        buttonPanel.add(showScheduleButton);
        buttonPanel.add(bookTicketsButton);
        buttonPanel.add(logoutButton);

        // Create top panel to hold welcome message and buttons
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(welcomeLabel, BorderLayout.NORTH);
        topPanel.add(buttonPanel, BorderLayout.CENTER);

        // Create main content panel with CardLayout
        contentPanel = new JPanel(new CardLayout());

        // Create scrollable cards panel for musicals (home view)
        JPanel cardsPanel = new JPanel(new GridLayout(0, 2, 15, 15));
        cardsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(cardsPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Create schedule panel
        JScrollPane schedulePane = new JScrollPane();
        musicalTextArea = new JTextArea(15, 40);
        musicalTextArea.setEditable(false);
        schedulePane.setViewportView(musicalTextArea);

        // Add both views to content panel
        contentPanel.add(scrollPane, "HOME");
        contentPanel.add(schedulePane, "SCHEDULE");

        // Action listeners
        homeButton.addActionListener(e -> {
            isShowingSchedule = false;
            ((CardLayout) contentPanel.getLayout()).show(contentPanel, "HOME");
            updateMusicalCards(cardsPanel, (String) categoryDropdown.getSelectedItem());
        });

        showScheduleButton.addActionListener(e -> {
            isShowingSchedule = true;
            ((CardLayout) contentPanel.getLayout()).show(contentPanel, "SCHEDULE");
            showFilteredSchedule((String) categoryDropdown.getSelectedItem());
        });

        bookTicketsButton.addActionListener(e -> 
            cardLayout.show(mainPanel, "BookingPanel"));

        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(null,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                currentUser = null;
                userRole = null;
                cardLayout.show(mainPanel, "SplashScreen");
            }
        });

        // Add buttons to panel
        buttonPanel.add(homeButton);
        buttonPanel.add(showScheduleButton);
        buttonPanel.add(bookTicketsButton);
        buttonPanel.add(logoutButton);

        // Create filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        filterPanel.add(new JLabel("Filter by Category:"));
        categoryDropdown = new JComboBox<>(getUniqueCategories());
        filterPanel.add(categoryDropdown);

        // Add category filter listener
        categoryDropdown.addActionListener(e -> {
            String selectedCategory = (String) categoryDropdown.getSelectedItem();
            if (isShowingSchedule) {
                showFilteredSchedule(selectedCategory);
            } else {
                updateMusicalCards(cardsPanel, selectedCategory);
            }
        });

        // Initial display
        updateMusicalCards(cardsPanel, "All");
        ((CardLayout) contentPanel.getLayout()).show(contentPanel, "HOME");

        // Assemble the panels
        musicalListPanel.add(topPanel, BorderLayout.NORTH);
        musicalListPanel.add(contentPanel, BorderLayout.CENTER);
        musicalListPanel.add(filterPanel, BorderLayout.SOUTH);

        return musicalListPanel;
    }


    private static void updateMusicalCards(JPanel cardsPanel, String category) {
        cardsPanel.removeAll();
        for (MusicalDataHandler.Musical musical : dataHandler.getMusicals()) {
            if (category.equals("All") || musical.getCategories().contains(category)) {
                cardsPanel.add(createMusicalCard(musical));
            }
        }
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    private static JPanel createMusicalCard(MusicalDataHandler.Musical musical) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 2, true),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(300, 200)); // Fixed size for cards

        // Title
        JLabel nameLabel = new JLabel(musical.getName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Details panel with proper spacing
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(Color.WHITE);
        
        String[] details = {
            "Runtime: " + musical.getRunTime(),
            "Categories: " + musical.getCategories(),
            "Age: " + musical.getAgeRestriction(),
            "Price: £" + String.format("%.2f", musical.getPrice())
        };

        for (String detail : details) {
            JLabel label = new JLabel(detail);
            label.setFont(new Font("Arial", Font.PLAIN, 14));
            label.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
            detailsPanel.add(label);
        }

        card.add(nameLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(detailsPanel);

        return card;
    }



    private static String getWelcomeMessage() {
        int hour = LocalDateTime.now().getHour();
        if (hour < 12) return "Good Morning";
        if (hour < 17) return "Good Afternoon";
        return "Good Evening";
    }

    
    private static JPanel createAdminMusicalListPanel() {
        JPanel musicalListPanel = new JPanel(new BorderLayout());

        // Panel for buttons at the top
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2, 10, 10));

        // Create buttons
        JButton musicalListButton = new JButton("Musical List");
        JButton showScheduleButton = new JButton("Show Schedule");

        // Add buttons to the button panel
        buttonPanel.add(musicalListButton);
        buttonPanel.add(showScheduleButton);

        // Create text area for musical list
        musicalTextArea = new JTextArea(15, 40);
        musicalTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(musicalTextArea);

        // Create category filter
        JLabel filterLabel = new JLabel("Filter by Category:");
        categoryDropdown = new JComboBox<>(getUniqueCategories());

        // Add ActionListener for the dropdown
        categoryDropdown.addActionListener(e -> {
            String selectedCategory = (String) categoryDropdown.getSelectedItem();
            if (isShowingSchedule) {
                showFilteredSchedule(selectedCategory);
            } else {
                showFilteredMusicalList(selectedCategory);
            }
        });

        showScheduleButton.addActionListener(e -> {
            isShowingSchedule = true;
            showFilteredSchedule((String) categoryDropdown.getSelectedItem());
        });

        musicalListButton.addActionListener(e -> {
            isShowingSchedule = false;
            showFilteredMusicalList((String) categoryDropdown.getSelectedItem());
        });

        // Create filter panel
        JPanel filterPanel = new JPanel();
        filterPanel.add(filterLabel);
        filterPanel.add(categoryDropdown);

        // Assemble the panel
        musicalListPanel.add(buttonPanel, BorderLayout.NORTH);
        musicalListPanel.add(filterPanel, BorderLayout.SOUTH);
        musicalListPanel.add(scrollPane, BorderLayout.CENTER);

        // Initially display all musicals
        musicalTextArea.setText(getAllMusicalsText());

        return musicalListPanel;
    }


    // Method to retrieve all musicals in text format
    private static String getAllMusicalsText() {
        StringBuilder musicalsText = new StringBuilder();
        for (MusicalDataHandler.Musical musical : dataHandler.musicals) {
            musicalsText.append(formatMusicalInfo(musical)).append("\n\n");
        }
        return musicalsText.toString();
    }

    // Method to filter musicals by category
    private static String filterMusicalsByCategory(String category) {
        StringBuilder filteredText = new StringBuilder();
        for (MusicalDataHandler.Musical musical : dataHandler.musicals) {
            if (musical.getCategories().contains(category)) {
                filteredText.append(formatMusicalInfo(musical)).append("\n\n");
            }
        }
        return filteredText.length() > 0 ? filteredText.toString() : "No musicals found for this category.";
    }
    
    private static void showFilteredSchedule(String category) {
        StringBuilder scheduleText = new StringBuilder("Show Schedule:\n\n");
        try (Connection conn = DriverManager.getConnection(
                MusicalDataHandler.DB_URL, MusicalDataHandler.USER, MusicalDataHandler.PASS)) {
            String query = "SELECT m.*, GROUP_CONCAT(v.name) as venues " +
                          "FROM musicals m " +
                          "LEFT JOIN musical_venues mv ON m.id = mv.musical_id " +
                          "LEFT JOIN venues v ON mv.venue_id = v.id " +
                          "WHERE ? = 'All' OR m.categories LIKE ? " +
                          "GROUP BY m.id";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, category);
            pstmt.setString(2, "%" + category + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                scheduleText.append("Musical: ").append(rs.getString("name"))
                    .append("\nPrice: £").append(String.format("%.2f", rs.getDouble("price")))
                    .append("\nAvailable Days: ").append(rs.getString("available_days"));

                String venues = rs.getString("venues");
                if (venues != null && !venues.isEmpty()) {
                    scheduleText.append("\nVenues: ").append(venues);
                }
                scheduleText.append("\n\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            scheduleText.append("Error loading schedule data.");
        }

        musicalTextArea.setText(scheduleText.toString());
    }



    private static void showFilteredMusicalList(String category) {
        if (category.equals("All")) {
            musicalTextArea.setText(getAllMusicalsText());
        } else {
            musicalTextArea.setText(filterMusicalsByCategory(category));
        }
    }


    // Method to format musical information for display
    private static String formatMusicalInfo(MusicalDataHandler.Musical musical) {
        StringBuilder info = new StringBuilder(String.format("""
            %s
            Run time: %s
            Category: %s
            Age: %s
            Price: £%.2f
            """, musical.getName(), musical.getRunTime(), musical.getCategories(), 
            musical.getAgeRestriction(), musical.getPrice()));

        // Add venue information
        List<String> venues = getVenuesForMusical(musical.getId());
        if (!venues.isEmpty()) {
            info.append("Venues: ").append(String.join(", ", venues)).append("\n");
        }

        return info.toString();
    }

    private static List<String> getVenuesForMusical(int musicalId) {
        List<String> venues = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(
                dataHandler.DB_URL, dataHandler.USER, dataHandler.PASS)) {
            String query = "SELECT v.name FROM venues v " +
                          "JOIN musical_venues mv ON v.id = mv.venue_id " +
                          "WHERE mv.musical_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, musicalId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                venues.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return venues;
    }


    // Method to retrieve unique categories from the CSV
    private static String[] getUniqueCategories() {
        Set<String> categoriesSet = new HashSet<>();
        categoriesSet.add("All");  // Add "All" option for no filter

        for (MusicalDataHandler.Musical musical : dataHandler.musicals) {
            // Split categories by comma and add to the set
            String[] categories = musical.getCategories().split(", ");
            categoriesSet.addAll(Arrays.asList(categories));
        }

        // Convert the set to a sorted array for the dropdown
        return categoriesSet.toArray(new String[0]);
    }

    // Method to create the Booking Panel (booking GUI)
    private static JPanel createBookingPanel() {
        JPanel bookingPanel = new JPanel(new BorderLayout());
        JLabel ticketsCountLabel = new JLabel();

        // Create main input panel
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Musical selection
        inputPanel.add(new JLabel("Select Musical:"));
        JComboBox<String> musicalDropdown = new JComboBox<>();
        for (MusicalDataHandler.Musical musical : dataHandler.getMusicals()) {
            musicalDropdown.addItem(musical.getName());
        }
        inputPanel.add(musicalDropdown);

        SwingUtilities.invokeLater(() -> {
            String selected = (String)musicalDropdown.getSelectedItem();
            if (selected != null) {
                MusicalDataHandler.Musical musical = dataHandler.getMusicalByName(selected);
                if (musical != null) {
                    ticketsCountLabel.setText(String.valueOf(musical.getAvailableTickets()));
                }
            }
        });

        // Date and Time selection
        inputPanel.add(new JLabel("Select Date and Time:"));
        MusicalDataHandler.Musical selectedMusical = dataHandler.getMusicalByName(
            (String)musicalDropdown.getSelectedItem());
        DateTimeSelector dateTimeSelector = new DateTimeSelector(
            selectedMusical.getAvailableDays(),
            selectedMusical.getRunTime()
        );
        inputPanel.add(dateTimeSelector);

        // ticketsCountLabel.setText(String.valueOf(selectedMusical.getAvailableTickets()));

        // Available tickets display
        JLabel availableTicketsLabel = new JLabel("Available Tickets: ");
        inputPanel.add(availableTicketsLabel);
        inputPanel.add(ticketsCountLabel);

        // Initialize available tickets display for pre-selected musical
        String initialMusical = (String)musicalDropdown.getSelectedItem();
        if (initialMusical != null) {
            MusicalDataHandler.Musical musical = dataHandler.getMusicalByName(initialMusical);
            if (musical != null) {
                ticketsCountLabel.setText(String.valueOf(musical.getAvailableTickets()));
            }
        }

        // Create ticket selection panel
        JPanel ticketPanel = new JPanel();
        ticketPanel.setLayout(new BoxLayout(ticketPanel, BoxLayout.Y_AXIS));
        ticketPanel.setBorder(BorderFactory.createTitledBorder("Select Tickets"));

        // List to store ticket selections
        List<JPanel> ticketSelections = new ArrayList<>();

        // Add initial ticket selection row
        JButton addTicketButton = new JButton("Add Another Ticket Type");
        addTicketButton.addActionListener(e -> {
            JPanel ticketRow = createTicketSelectionRow();
            ticketSelections.add(ticketRow);
            ticketPanel.add(ticketRow);
            ticketPanel.revalidate();
            ticketPanel.repaint();
        });

        // Add first ticket row
        JPanel firstTicketRow = createTicketSelectionRow();
        ticketSelections.add(firstTicketRow);
        ticketPanel.add(firstTicketRow);
        ticketPanel.add(addTicketButton);

        // Button panel
        JPanel buttonPanel = new JPanel();
        JButton continueToSeatsButton = new JButton("Continue to Seat Selection");
        JButton bookTicketsButton = new JButton("Book Tickets");
        JButton backButton = new JButton("Back");

        bookTicketsButton.setVisible(false); // Initially hidden

        // Selected seats info area
        seatInfoTextArea = new JTextArea(5, 30);
        seatInfoTextArea.setEditable(false);
        JScrollPane seatInfoScroll = new JScrollPane(seatInfoTextArea);
        seatInfoScroll.setBorder(BorderFactory.createTitledBorder("Selected Seats"));
        seatInfoScroll.setVisible(false);

        List<MusicalDataHandler.BookedSeat> selectedSeats = new ArrayList<>();

        // Update available tickets display and DateTimeSelector when musical changes
        musicalDropdown.addActionListener(e -> {
            String selected = (String)musicalDropdown.getSelectedItem();
            MusicalDataHandler.Musical musical = dataHandler.getMusicalByName(selected);
            if (musical != null) {
                // Update available tickets display
                ticketsCountLabel.setText(String.valueOf(musical.getAvailableTickets()));

                // Update date time selector settings without resetting current selection
                dateTimeSelector.updateSettings(
                    musical.getAvailableDays(),
                    musical.getRunTime()
                );

                // Reset ticket selections
                ticketPanel.removeAll();
                ticketSelections.clear();
                JPanel newTicketRow = createTicketSelectionRow();
                ticketSelections.add(newTicketRow);
                ticketPanel.add(newTicketRow);
                ticketPanel.add(addTicketButton);

                // Reset seat selections
                selectedSeats.clear();
                seatInfoTextArea.setText("");
                seatInfoScroll.setVisible(false);

                // Reset buttons to initial state
                continueToSeatsButton.setVisible(true);
                bookTicketsButton.setVisible(false);

                // Refresh panel
                ticketPanel.revalidate();
                ticketPanel.repaint();
            }
        });

        continueToSeatsButton.addActionListener(e -> {
            String musicalName = (String) musicalDropdown.getSelectedItem();
            LocalDateTime selectedDateTime = dateTimeSelector.getSelectedDateTime();

            if (selectedDateTime == null) {
                JOptionPane.showMessageDialog(null, "Please select a date and time");
                return;
            }

            int totalTickets = calculateTotalTickets(ticketSelections);
            if (!validateTicketSelection(totalTickets, musicalName, selectedDateTime)) {
                return;
            }

            // Create and show seat selection dialog
            JDialog seatDialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(mainPanel),
                "Select Seats", true);
            SeatSelectionPanel seatPanel = new SeatSelectionPanel(
                dataHandler,
                musicalName,
                selectedDateTime.toLocalTime().toString(),
                totalTickets,
                selectedDateTime.toLocalDate()
            );

            JPanel dialogButtonPanel = new JPanel();
            JButton dialogBackButton = new JButton("Back");
            JButton confirmButton = new JButton("Confirm Selection");

            dialogBackButton.addActionListener(evt -> seatDialog.dispose());

            confirmButton.addActionListener(evt -> {
                List<MusicalDataHandler.BookedSeat> seats = seatPanel.getSelectedSeats();
                if (seats == null || seats.size() != totalTickets) {
                    JOptionPane.showMessageDialog(seatDialog,
                        "Please select exactly " + totalTickets + " seats.");
                    return;
                }

                selectedSeats.clear();
                selectedSeats.addAll(seats);
                updateSelectedSeatsDisplay(selectedSeats);
                seatInfoScroll.setVisible(true);
                continueToSeatsButton.setVisible(false);
                bookTicketsButton.setVisible(true);

                seatDialog.dispose();
            });

            dialogButtonPanel.add(dialogBackButton);
            dialogButtonPanel.add(confirmButton);

            seatDialog.setLayout(new BorderLayout());
            seatDialog.add(seatPanel, BorderLayout.CENTER);
            seatDialog.add(dialogButtonPanel, BorderLayout.SOUTH);
            seatDialog.setSize(800, 600);
            seatDialog.setLocationRelativeTo(null);
            seatDialog.setVisible(true);
        });

        bookTicketsButton.addActionListener(e -> {
            if (selectedSeats.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please select seats first.");
                return;
            }

            // Get the CURRENT selected musical (not the initial one)
            String currentMusicalName = (String) musicalDropdown.getSelectedItem();
            LocalDateTime selectedDateTime = dateTimeSelector.getSelectedDateTime();
            MusicalDataHandler.Musical currentMusical = dataHandler.getMusicalByName(currentMusicalName);

            if (currentMusical == null) {
                JOptionPane.showMessageDialog(null, "Error: Musical not found.");
                return;
            }

            if (processBooking(currentMusical, selectedDateTime, ticketSelections, selectedSeats)) {
                // Reset UI
                selectedSeats.clear();
                seatInfoTextArea.setText("");
                seatInfoScroll.setVisible(false);
                continueToSeatsButton.setVisible(true);
                bookTicketsButton.setVisible(false);

                // Reset date time selector
                dateTimeSelector.reset();

                // Reset musical dropdown
                musicalDropdown.setSelectedIndex(0);

                // Reset ticket selections
                ticketPanel.removeAll();
                ticketSelections.clear();
                JPanel newTicketRow = createTicketSelectionRow();
                ticketSelections.add(newTicketRow);
                ticketPanel.add(newTicketRow);
                ticketPanel.add(addTicketButton);

                // Update available tickets display for the initial musical after reset
                String resetMusical = (String)musicalDropdown.getSelectedItem();
                MusicalDataHandler.Musical musical = dataHandler.getMusicalByName(resetMusical);
                if (musical != null) {
                    ticketsCountLabel.setText(String.valueOf(musical.getAvailableTickets()));
                }

                ticketPanel.revalidate();
                ticketPanel.repaint();
            }
        });



        backButton.addActionListener(e -> {
            // Reset all form elements
            resetBookingForm(musicalDropdown, dateTimeSelector, ticketPanel, 
                            ticketSelections, addTicketButton, seatInfoScroll, 
                            continueToSeatsButton, bookTicketsButton, ticketsCountLabel
                            );

            cardLayout.show(mainPanel, "MusicalListPanel");
        });

        buttonPanel.add(continueToSeatsButton);
        buttonPanel.add(bookTicketsButton);
        buttonPanel.add(backButton);

        // Assemble final panel
        bookingPanel.add(inputPanel, BorderLayout.NORTH);
        bookingPanel.add(new JScrollPane(ticketPanel), BorderLayout.CENTER);
        bookingPanel.add(seatInfoScroll, BorderLayout.EAST);
        bookingPanel.add(buttonPanel, BorderLayout.SOUTH);

        return bookingPanel;
    }
   
   private static void resetBookingForm(JComboBox<String> musicalDropdown, 
                                   DateTimeSelector dateTimeSelector,
                                   JPanel ticketPanel,
                                   List<JPanel> ticketSelections,
                                   JButton addTicketButton,
                                   JScrollPane seatInfoScroll,
                                   JButton continueToSeatsButton,
                                   JButton bookTicketsButton,
                                   JLabel ticketsCountLabel) {
        // Reset musical selection
        musicalDropdown.setSelectedIndex(0);

        // Reset date time selector
        dateTimeSelector.reset();

        // Reset ticket selections
        ticketPanel.removeAll();
        ticketSelections.clear();
        JPanel newTicketRow = createTicketSelectionRow();
        ticketSelections.add(newTicketRow);
        ticketPanel.add(newTicketRow);
        ticketPanel.add(addTicketButton);

        // Reset seat info
        seatInfoScroll.setVisible(false);
        if (seatInfoTextArea != null) {
            seatInfoTextArea.setText("");
        }

        // Reset buttons
        continueToSeatsButton.setVisible(true);
        bookTicketsButton.setVisible(false);

        // Reset ticket count display
        ticketsCountLabel.setText("");

        // Refresh panel
        ticketPanel.revalidate();
        ticketPanel.repaint();
    }


    private JPanel createShowTimesPanel(String runtime) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Show Times"));

        DefaultListModel<java.time.LocalTime> timeListModel = new DefaultListModel<>();
        JList<java.time.LocalTime> timeList = new JList<>(timeListModel);

        DateTimeSelector timeSelector = new DateTimeSelector("", runtime);
        JButton addTimeButton = new JButton("Add Time");

        addTimeButton.addActionListener(e -> {
            LocalDateTime selected = timeSelector.getSelectedDateTime();
            if (selected != null) {
                java.time.LocalTime newTime = selected.toLocalTime();

                // Validate time against existing times
                if (isTimeValid(timeListModel, newTime, runtime)) {
                    timeListModel.addElement(newTime);
                } else {
                    JOptionPane.showMessageDialog(panel, 
                        "Invalid time slot. Ensure proper spacing between shows.");
                }
            }
        });

        panel.add(new JScrollPane(timeList), BorderLayout.CENTER);
        panel.add(timeSelector, BorderLayout.NORTH);
        panel.add(addTimeButton, BorderLayout.SOUTH);

        return panel;
    }

    private boolean isTimeValid(DefaultListModel<java.time.LocalTime> model, java.time.LocalTime newTime, String runtime) {
        int runtimeMinutes = parseRuntime(runtime);

        for (int i = 0; i < model.size(); i++) {
            java.time.LocalTime existingTime = model.getElementAt(i);

            // Check if new time conflicts with existing show time
            if (Math.abs(java.time.Duration.between(existingTime, newTime).toMinutes()) < runtimeMinutes) {
                return false;
            }
        }
        return true;
    }

    private int parseRuntime(String runtime) {
        int minutes = 0;
        String[] parts = runtime.split(" ");
        for (String part : parts) {
            if (part.endsWith("h")) {
                minutes += Integer.parseInt(part.replace("h", "")) * 60;
            } else if (part.endsWith("min")) {
                minutes += Integer.parseInt(part.replace("min", ""));
            }
        }
        return minutes;
    }

    
    private static void updateSelectedSeatsDisplay(List<MusicalDataHandler.BookedSeat> seats) {
        if (seatInfoTextArea == null) {
            seatInfoTextArea = new JTextArea(5, 30);
            seatInfoTextArea.setEditable(false);
            seatInfoTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        }

        StringBuilder seatInfo = new StringBuilder();
        seatInfo.append("Selected Seats:\n");
        seatInfo.append("------------------\n");

        double totalPrice = 0.0;
        for (MusicalDataHandler.BookedSeat seat : seats) {
            seatInfo.append(String.format("Seat %-8s - £%6.2f\n", 
                seat.getSeatNumber(), seat.getPrice()));
            totalPrice += seat.getPrice();
        }

        seatInfo.append("------------------\n");
        seatInfo.append(String.format("Total Price:    £%6.2f\n", totalPrice));

        seatInfoTextArea.setText(seatInfo.toString());
        seatInfoTextArea.setVisible(true);
    }

    private static void clearSelectedSeatsDisplay() {
        if (seatInfoTextArea != null) {
            seatInfoTextArea.setText("");
            seatInfoTextArea.setVisible(false);
        }
    }
    
    private static boolean validateTicketSelection(int totalTickets, String musicalName, 
        LocalDateTime selectedDateTime) {
        if (totalTickets == 0) {
            JOptionPane.showMessageDialog(null, "Please enter valid ticket quantities.");
            return false;
        }

        MusicalDataHandler.Musical musical = dataHandler.getMusicalByName(musicalName);
        if (musical == null) {
            JOptionPane.showMessageDialog(null, "Musical not found.");
            return false;
        }

        if (totalTickets > musical.getAvailableTickets()) {
            JOptionPane.showMessageDialog(null, "Not enough tickets available.");
            return false;
        }

        String selectedDay = selectedDateTime.getDayOfWeek().toString();
        String availableDaysStr = musical.getAvailableDays().replace("\"", "");

        // Convert both to uppercase for comparison
        if (!Arrays.stream(availableDaysStr.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .anyMatch(day -> day.equals(selectedDay.toUpperCase()))) {
            JOptionPane.showMessageDialog(null,
                "This show is not available on " + selectedDay + ".\n" +
                "Available days are: " + availableDaysStr);
            return false;
        }

        return true;
    }


    
    private static boolean processBooking(MusicalDataHandler.Musical musical,
                                    LocalDateTime showDateTime,
                                    List<JPanel> ticketSelections,
                                    List<MusicalDataHandler.BookedSeat> selectedSeats) {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(MusicalDataHandler.DB_URL, MusicalDataHandler.USER, MusicalDataHandler.PASS);
            conn.setAutoCommit(false);

            // Calculate total price and build receipt details
            double totalPrice = 0;
            StringBuilder receiptDetails = new StringBuilder();
            int seatIndex = 0;

            for (JPanel ticketRow : ticketSelections) {
                JComboBox<?> typeDropdown = (JComboBox<?>) ticketRow.getComponent(1);
                JTextField countField = (JTextField) ticketRow.getComponent(3);
                String ticketType = (String) typeDropdown.getSelectedItem();
                int count = Integer.parseInt(countField.getText());

                for (int i = 0; i < count; i++) {
                    MusicalDataHandler.BookedSeat seat = selectedSeats.get(seatIndex++);
                    double seatPrice = calculatePrice(seat.getPrice(), ticketType, 1);
                    totalPrice += seatPrice;
                    receiptDetails.append(String.format("%s ticket - Seat %s: £%.2f\n",
                        ticketType, seat.getSeatNumber(), seatPrice));
                }
            }

            // Insert receipt record
            String receiptQuery = "INSERT INTO receipts (customer_id, musical_id, total_price, show_date, show_time, receipt_text) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement receiptStmt = conn.prepareStatement(receiptQuery, Statement.RETURN_GENERATED_KEYS);
            
            // Format the data to be inserted
            int customerId = dataHandler.getCustomerId(currentUser);
            receiptStmt.setInt(1, customerId);                                                              // Customer ID
            receiptStmt.setInt(2, musical.getId());                                                         // Musical ID
            receiptStmt.setDouble(3, totalPrice);                                                           // Total Price
            receiptStmt.setDate(4, java.sql.Date.valueOf(showDateTime.toLocalDate()));                      // Show Date
            receiptStmt.setString(5, showDateTime.format(DateTimeFormatter.ofPattern("HH:mm")));    // Show time
            receiptStmt.setString(6, receiptDetails.toString());                                            // Receipt Text
            
            receiptStmt.executeUpdate();
            ResultSet rs = receiptStmt.getGeneratedKeys();
            
            int receiptId = 0;
            if (rs.next()) {
                receiptId = rs.getInt(1);
            }

            // Store booked seats with receipt ID
            String seatQuery = "INSERT INTO booked_seats (musical_id, venue_id, section_id, seat_number, show_date, show_time, booking_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement seatStmt = conn.prepareStatement(seatQuery);
            
            for (MusicalDataHandler.BookedSeat seat : selectedSeats) {
                seatStmt.setInt(1, musical.getId());
                seatStmt.setInt(2, seat.getVenueId());
                seatStmt.setInt(3, seat.getSectionId());
                seatStmt.setString(4, seat.getSeatNumber());
                seatStmt.setDate(5, java.sql.Date.valueOf(showDateTime.toLocalDate()));
                seatStmt.setString(6, showDateTime.format(DateTimeFormatter.ofPattern("HH:mm")));
                seatStmt.setInt(7, receiptId);
                seatStmt.executeUpdate();
            }

            // Store income data
            String incomeQuery = "INSERT INTO income_data (amount, transaction_date, category, description) VALUES (?, ?, ?, ?)";
            PreparedStatement incomeStmt = conn.prepareStatement(incomeQuery);
            incomeStmt.setDouble(1, totalPrice);
            incomeStmt.setDate(2, java.sql.Date.valueOf(showDateTime.toLocalDate()));
            incomeStmt.setString(3, "Ticket Sales");
            incomeStmt.setString(4, "Booking for " + musical.getName());
            incomeStmt.executeUpdate();

            conn.commit();            
            
            // Generate receipt and display success message
            String receipt = generateMultiTicketReceipt(musical.getName(), showDateTime, receiptDetails.toString(), totalPrice);
            JOptionPane.showMessageDialog(null, "Booking Successful!\n" + receipt);
            return true;

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage());
            return false;
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    private static boolean isDayAvailable(String availableDays, String currentDay) {
        return Arrays.stream(availableDays.split(","))
            .map(String::trim)
            .anyMatch(day -> day.equalsIgnoreCase(currentDay));
    }

    private static int calculateTotalTickets(List<JPanel> ticketSelections) {
        return ticketSelections.stream()
            .mapToInt(row -> {
                try {
                    return Integer.parseInt(((JTextField)row.getComponent(3)).getText());
                } catch (NumberFormatException e) {
                    return 0;
                }
            })
            .sum();
    }

    private static JPanel createTicketSelectionRow() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Ticket Type:"));
        panel.add(new JComboBox<>(new String[]{"Adult", "Senior", "Student"}));
        panel.add(new JLabel("Quantity:"));
        panel.add(new JTextField(5));
        return panel;
    }

    //  price calculation method
    private static double calculatePrice(double basePrice, String ticketType, int ticketCount) {
        // Apply discount based on ticket type
        double discount = switch (ticketType) {
            case "Senior" -> 0.3; // 30% off
            case "Student" -> 0.6; // 60% off
            default -> 0.0;  // No discount for Adult
        };

        // Final price calculation
        return basePrice * ticketCount * (1 - discount);
    }

    private static String generateMultiTicketReceipt(String musicalName, LocalDateTime showDateTime,
        String ticketDetails, double totalPrice) {

        LocalDateTime currentDateTime = LocalDateTime.now();
        String formattedDateTime = currentDateTime.format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String receipt = String.format("""
            Date and Time: %s
            Musical: %s
            Show Time: %s
            Ticket Details:
            %s
            Total Price: £%.2f
            --------------------------------------
            """, formattedDateTime, musicalName, showDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), ticketDetails, totalPrice);

        // Save to file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("receipt.txt", true))) {
            writer.write(receipt);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return receipt;
    }

     
}
