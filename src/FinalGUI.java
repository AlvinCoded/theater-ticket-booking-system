import javax.swing.*;
import java.awt.*;
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

    public static void main(String[] args) throws IOException {
        // Load musical data from the CSV file
        dataHandler = new MusicalDataHandler("resources/musicals.csv");

        // Create the main frame
        JFrame frame = new JFrame("London Musical Tickets");
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
    }

    
    // Method to create the initial splash screen with background image
    private static JPanel createSplashScreen() {
        // Create a panel for the splash screen
        JPanel splashPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Load and draw the background image
                try {
                    Image bgImage = ImageIO.read(new File("resources/splash-screen.png"));
                    g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        splashPanel.setLayout(new GridBagLayout());

        // Create the "Go" button
        JButton goButton = new JButton("Go!") {
            @Override
            protected void paintComponent(Graphics g) {
                if (getModel().isArmed()) {
                    g.setColor(Color.GREEN);
                } else {
                    g.setColor(new Color(255, 255, 255, 250));
                }
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                super.paintComponent(g);
            }
        };

        goButton.setFont(new Font("Arial", Font.BOLD, 24));
        goButton.setForeground(Color.BLACK);
        goButton.setBorderPainted(false);
        goButton.setFocusPainted(false);
        goButton.setContentAreaFilled(false);

        // Set button size
        goButton.setPreferredSize(new Dimension(100, 80));

        // Add action listener to the "Go" button
        goButton.addActionListener(e -> {
            cardLayout.show(mainPanel, "MusicalListPanel");
        });

        // Add the button to the splash panel
        splashPanel.add(goButton);

        return splashPanel;
    }

    private static JTextArea musicalTextArea;
    private static JComboBox<String> categoryDropdown;
    private static boolean isShowingSchedule = false;
    
    // Method to create the Musical List Panel with Category Filter
    private static JPanel createMusicalListPanel() {
        JPanel musicalListPanel = new JPanel(new BorderLayout());

        // Panel creation for buttons at the top
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 4, 10, 10));

        // Create buttons
        JButton musicalListButton = new JButton("Musical List");
        JButton showScheduleButton = new JButton("Show schedule");
        JButton bookTicketsButton = new JButton("Book Tickets");
        JButton exitButton = new JButton("Exit");

        // Action listener to switch to booking panel
        bookTicketsButton.addActionListener(e -> cardLayout.show(mainPanel, "BookingPanel"));

        // Add action listener to Exit button to close the application
        exitButton.addActionListener(e -> System.exit(0));

        // Add buttons to the button panel
        buttonPanel.add(musicalListButton);
        buttonPanel.add(showScheduleButton);
        buttonPanel.add(bookTicketsButton);
        buttonPanel.add(exitButton);

        // Create a text area to display the musical list
        musicalTextArea = new JTextArea(15, 40);
        musicalTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(musicalTextArea);

        // Create a dropdown for filtering by category
        JLabel filterLabel = new JLabel("Filter by Category:");
        categoryDropdown = new JComboBox<>(getUniqueCategories());

        // Add ActionListener for the dropdown to filter musicals
        categoryDropdown.addActionListener(e -> {
            String selectedCategory = (String) categoryDropdown.getSelectedItem();
            // Filter the musical list by the selected category based on current view
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


        // Create a panel to hold the filter dropdown
        JPanel filterPanel = new JPanel();
        filterPanel.add(filterLabel);
        filterPanel.add(categoryDropdown);

        // Add the button panel, filter panel, and text area to the main panel
        musicalListPanel.add(buttonPanel, BorderLayout.NORTH);
        musicalListPanel.add(filterPanel, BorderLayout.SOUTH);
        musicalListPanel.add(scrollPane, BorderLayout.CENTER);

        // Initially, display all musicals
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
    for (MusicalDataHandler.Musical musical : dataHandler.musicals) {
        if (category.equals("All") || musical.getCategories().contains(category)) {
            scheduleText.append("Musical: ").append(musical.getName())
                    .append("\nPrice: £").append(musical.getPrice())
                    .append("\nShow Times: 2:00 PM, 6:00 PM, 8:00 PM")
                    .append("\nAvailable Days: ").append(musical.getAvailableDays().replace("\"", ""))
                    .append("\n\n");
        }
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
        return String.format("""
            %s
            Run time: %s
            Category: %s
            Venue: %s
            Age: %s
            Price: £%.2f
            """, musical.getName(), musical.getRunTime(), musical.getCategories(),
            musical.getVenue(), musical.getAgeRestriction(), musical.getPrice());
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

        // Create main input panel
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));

        // Musical selection
        inputPanel.add(new JLabel("Select Musical:"));
        JComboBox<String> musicalDropdown = new JComboBox<>();
        try {
            for (MusicalDataHandler.Musical musical : dataHandler.musicals) {
                musicalDropdown.addItem(musical.getName());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error loading musical names.");
        }
        inputPanel.add(musicalDropdown);

        // Show time selection
        inputPanel.add(new JLabel("Select Show Time:"));
        JComboBox<String> showTimeDropdown = new JComboBox<>(new String[]{"2:00 PM", "6:00 PM", "8:00 PM"});
        inputPanel.add(showTimeDropdown);

        // Available tickets display
        JLabel availableTicketsLabel = new JLabel("Available Tickets: ");
        JLabel ticketsCountLabel = new JLabel();
        inputPanel.add(availableTicketsLabel);
        inputPanel.add(ticketsCountLabel);

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
        JButton bookButton = new JButton("Book Tickets");
        JButton backButton = new JButton("Back");

        // Update available tickets display
        Runnable updateAvailableTickets = () -> {
            String selectedMusical = (String) musicalDropdown.getSelectedItem();
            MusicalDataHandler.Musical musical = dataHandler.getMusicalByName(selectedMusical);
            if (musical != null) {
                ticketsCountLabel.setText(String.valueOf(musical.getAvailableTickets()));
            }
        };

        SwingUtilities.invokeLater(updateAvailableTickets);
        musicalDropdown.addActionListener(e -> updateAvailableTickets.run());

        // Book button action
        bookButton.addActionListener(e -> {
            String musicalName = (String) musicalDropdown.getSelectedItem();
            String showTime = (String) showTimeDropdown.getSelectedItem();
            MusicalDataHandler.Musical selectedMusical = dataHandler.getMusicalByName(musicalName);

            if (selectedMusical == null) {
                JOptionPane.showMessageDialog(null, "Musical not found.");
                return;
            }

            // Validate day availability
            LocalDate currentDate = LocalDate.now();
            String currentDay = currentDate.format(DateTimeFormatter.ofPattern("EEEE"));
            String availableDaysStr = selectedMusical.getAvailableDays().replace("\"", "");
            boolean isAvailableToday = Arrays.stream(availableDaysStr.split(","))
                    .map(String::trim)
                    .anyMatch(day -> day.equalsIgnoreCase(currentDay));

            if (!isAvailableToday) {
                JOptionPane.showMessageDialog(null, 
                    "This show is not available today (" + currentDay + ").\n" +
                    "Available days are: " + availableDaysStr);
                return;
            }

            // Calculate total tickets and price
            int totalTickets = 0;
            double totalPrice = 0;
            StringBuilder receiptDetails = new StringBuilder();

            for (JPanel ticketRow : ticketSelections) {
                JComboBox<String> typeDropdown = (JComboBox<String>) ticketRow.getComponent(1);
                JTextField countField = (JTextField) ticketRow.getComponent(3);

                try {
                    String ticketType = (String) typeDropdown.getSelectedItem();
                    int count = Integer.parseInt(countField.getText());

                    if (count <= 0) continue;

                    totalTickets += count;
                    double price = calculatePrice(selectedMusical.getPrice(), ticketType, count);
                    totalPrice += price;

                    receiptDetails.append(String.format("%s tickets: %d (£%.2f)\n", 
                        ticketType, count, price));
                } catch (NumberFormatException ex) {
                    continue;
                }
            }

            if (totalTickets == 0) {
                JOptionPane.showMessageDialog(null, "Please enter valid ticket quantities.");
                return;
            }

            if (totalTickets > selectedMusical.getAvailableTickets()) {
                JOptionPane.showMessageDialog(null, "Not enough tickets available. Only " + 
                    selectedMusical.getAvailableTickets() + " tickets left.");
                return;
            }

            // Update available tickets
            selectedMusical.setAvailableTickets(selectedMusical.getAvailableTickets() - totalTickets);

            // Generate receipt
            String receipt = generateMultiTicketReceipt(musicalName, showTime, receiptDetails.toString(), totalPrice);
            JOptionPane.showMessageDialog(null, "Booking Successful!\n" + receipt);

            updateAvailableTickets.run();
        });

        backButton.addActionListener(e -> cardLayout.show(mainPanel, "MusicalListPanel"));

        buttonPanel.add(bookButton);
        buttonPanel.add(backButton);

        // Assemble final panel
        bookingPanel.add(inputPanel, BorderLayout.NORTH);
        bookingPanel.add(new JScrollPane(ticketPanel), BorderLayout.CENTER);
        bookingPanel.add(buttonPanel, BorderLayout.SOUTH);

        return bookingPanel;
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

    private static String generateMultiTicketReceipt(String musicalName, String showTime, 
        String ticketDetails, double totalPrice) 
    {
        LocalDateTime currentDateTime = LocalDateTime.now();
        String formattedDateTime = currentDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String receipt = String.format("""
                Date and Time: %s
                Musical: %s
                Show Time: %s
                Ticket Details:
                %s
                Total Price: £%.2f
                --------------------------------------
                """, formattedDateTime, musicalName, showTime, ticketDetails, totalPrice);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("receipt.txt", true))) {
            writer.write(receipt);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return receipt;
    }
     
}

