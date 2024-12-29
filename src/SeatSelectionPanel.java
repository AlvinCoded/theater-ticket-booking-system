import java.awt.*;
import java.util.*;
import javax.swing.JLabel;
import javax.swing.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class SeatSelectionPanel extends JPanel {
    private final MusicalDataHandler dataHandler;
    private final String musicalName;
    private final String showTime;
    private final int totalTickets;
    private final Map<JToggleButton, SeatInfo> seatButtons;
    private int selectedSeatsCount;
    private final JLabel selectionCountLabel;
    private final LocalDate showDate;
    
    public SeatSelectionPanel(MusicalDataHandler dataHandler, String musicalName, 
                            String showTime, int totalTickets, LocalDate showDate) {
        this.dataHandler = dataHandler;
        this.musicalName = musicalName;
        this.showTime = showTime;
        this.totalTickets = totalTickets;
        this.showDate = showDate;
        this.seatButtons = new HashMap<>();
        this.selectedSeatsCount = 0;
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(new JLabel("Select " + totalTickets + " seats"), BorderLayout.WEST);
        selectionCountLabel = new JLabel("Selected: 0/" + totalTickets);
        headerPanel.add(selectionCountLabel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);
        
        // Create scrollable seat layout
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        
        // Get venues and sections for this musical
        java.util.List<MusicalDataHandler.VenueWithSections> venues = dataHandler.getVenuesForMusical(musicalName);
        for (MusicalDataHandler.VenueWithSections venue : venues) {
            mainPanel.add(createVenuePanel(venue));
            mainPanel.add(Box.createVerticalStrut(20));
        }
        
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
        
        // Create legend
        add(createLegendPanel(), BorderLayout.SOUTH);
        
        
    }
    
    private JPanel createVenuePanel(MusicalDataHandler.VenueWithSections venue) {
        JPanel venuePanel = new JPanel(new BorderLayout());
        venuePanel.setBorder(BorderFactory.createTitledBorder(venue.getName()));
        
        JPanel sectionsPanel = new JPanel(new GridLayout(0, 1, 5, 15));
        
        for (MusicalDataHandler.VenueSection section : venue.getSections()) {
            JPanel sectionPanel = createSectionPanel(section, venue.getId());
            sectionsPanel.add(sectionPanel);
        }
        
        venuePanel.add(sectionsPanel, BorderLayout.CENTER);
        return venuePanel;
    }
    
    private JPanel createSectionPanel(MusicalDataHandler.VenueSection section, int venueId) {
        JPanel sectionPanel = new JPanel(new BorderLayout());
        sectionPanel.setBorder(BorderFactory.createTitledBorder(
            section.getName() + " - £" + section.getBasePrice()));
        
        JPanel seatsPanel = new JPanel(new GridLayout(0, 10, 2, 2));
        java.util.List<String> bookedSeats = dataHandler.getBookedSeats(
            musicalName, venueId, section.getId(), showDate, showTime);
        
        for (int i = 1; i <= section.getCapacity(); i++) {
            String seatNumber = String.format("%s%d", section.getName(), i);
            JToggleButton seatButton = new JToggleButton();
            seatButton.setPreferredSize(new Dimension(40, 40));
            
            if (bookedSeats.contains(seatNumber)) {
                seatButton.setEnabled(false);
                seatButton.setBackground(Color.RED);
            } else {
                seatButton.addActionListener(e -> handleSeatSelection(seatButton));
            }
            
            seatButtons.put(seatButton, new SeatInfo(seatNumber, section, venueId));
            seatsPanel.add(seatButton);
        }
        
        sectionPanel.add(seatsPanel, BorderLayout.CENTER);
        return sectionPanel;
        
    }
    
    private void handleSeatSelection(JToggleButton seatButton) {
        if (seatButton.isSelected()) {
            if (selectedSeatsCount >= totalTickets) {
                seatButton.setSelected(false);
                JOptionPane.showMessageDialog(this, 
                    "You can only select " + totalTickets + " seats.");
                return;
            }
            selectedSeatsCount++;
            seatButton.setBackground(Color.GREEN);
        } else {
            selectedSeatsCount--;
            seatButton.setBackground(null);
        }
        selectionCountLabel.setText("Selected: " + selectedSeatsCount + "/" + totalTickets);
    }
    
    private JPanel createLegendPanel() {
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        legendPanel.add(createLegendItem("Available", null));
        legendPanel.add(createLegendItem("Selected", Color.GREEN));
        legendPanel.add(createLegendItem("Booked", Color.RED));
        return legendPanel;
    }
    
    private JPanel createLegendItem(String text, Color color) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JButton sample = new JButton();
        sample.setPreferredSize(new Dimension(20, 20));
        sample.setBackground(color);
        sample.setEnabled(false);
        item.add(sample);
        item.add(new JLabel(text));
        return item;
    }
    
    public java.util.List<MusicalDataHandler.BookedSeat> getSelectedSeats() {
        if (selectedSeatsCount != totalTickets) {
            return null;
        }

        java.util.List<MusicalDataHandler.BookedSeat> selectedSeats = new ArrayList<>();
        for (Map.Entry<JToggleButton, SeatInfo> entry : seatButtons.entrySet()) {
            if (entry.getKey().isSelected()) {
                SeatInfo info = entry.getValue();
                selectedSeats.add(new MusicalDataHandler.BookedSeat(
                    info.seatNumber,
                    info.section.getBasePrice(),
                    info.venueId,
                    info.section.getId()
                ));
            }
        }
        return selectedSeats;
    }

    
    private static class SeatInfo {
        final String seatNumber;
        final MusicalDataHandler.VenueSection section;
        final int venueId;
        
        SeatInfo(String seatNumber, MusicalDataHandler.VenueSection section, int venueId) {
            this.seatNumber = seatNumber;
            this.section = section;
            this.venueId = venueId;
        }
    }
    
    
    private void checkAndShowFullyBookedMessage() {
        int totalAvailableSeats = 0;
        for (Map.Entry<JToggleButton, SeatInfo> entry : seatButtons.entrySet()) {
            if (entry.getKey().isEnabled()) {
                totalAvailableSeats++;
            }
        }

        if (totalAvailableSeats == 0) {
            JPanel messagePanel = new JPanel();
            messagePanel.setBackground(new Color(255, 200, 200));
            JLabel messageLabel = new JLabel("This venue is fully booked for the selected date and time");
            messageLabel.setForeground(Color.RED);
            messageLabel.setFont(new Font("Arial", Font.BOLD, 14));
            messagePanel.add(messageLabel);
            add(messagePanel, BorderLayout.SOUTH);
            revalidate();
        }
    }
    
    private void createMainPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        
        // Get venues and sections for this musical
        java.util.List<MusicalDataHandler.VenueWithSections> venues = dataHandler.getVenuesForMusical(musicalName);
        boolean allVenuesBooked = true;
        
        for (MusicalDataHandler.VenueWithSections venue : venues) {
            JPanel venuePanel = createVenuePanel(venue);
            mainPanel.add(venuePanel);
            mainPanel.add(Box.createVerticalStrut(20));
            
            // Check if this venue has any available seats
            if (!isVenueFullyBooked(venue)) {
                allVenuesBooked = false;
            }
        }
        
        // Add fully booked message if necessary
        if (allVenuesBooked) {
            JPanel messagePanel = new JPanel();
            JLabel messageLabel = new JLabel("All venues are fully booked for this date and time");
            messageLabel.setForeground(Color.RED);
            messageLabel.setFont(messageLabel.getFont().deriveFont(Font.BOLD));
            messagePanel.add(messageLabel);
            mainPanel.add(messagePanel);
        }
        
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private boolean isVenueFullyBooked(MusicalDataHandler.VenueWithSections venue) {
        int totalSeats = venue.getSections().stream()
            .mapToInt(MusicalDataHandler.VenueSection::getCapacity)
            .sum();
            
        java.util.List<String> bookedSeats = dataHandler.getBookedSeats(
            musicalName, venue.getId(), 0, showDate, showTime);
            
        return bookedSeats.size() >= totalSeats;
    }
    
}
