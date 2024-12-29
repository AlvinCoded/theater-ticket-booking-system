import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.jdatepicker.impl.*;
import org.jdatepicker.DateModel;


public class BookingHistoryPanel extends JPanel {
    private final MusicalDataHandler dataHandler;
    private JTable bookingsTable;
    private DefaultTableModel tableModel;
    private DateTimeSelector dateRangeSelector;
    
    public BookingHistoryPanel(MusicalDataHandler dataHandler) {
        this.dataHandler = dataHandler;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        createTopPanel();
        createMainContent();
        refreshBookingsTable(null, null); // Initially show all bookings
    }
    
    private void createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        
        // Create date range selector
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dateRangeSelector = new DateTimeSelector("", "");
        JButton filterButton = new JButton("Filter");
        JButton resetButton = new JButton("Show All");
        
        filterButton.addActionListener(e -> {
            LocalDateTime selectedDateTime = dateRangeSelector.getSelectedDateTime();
            if (selectedDateTime != null) {
                refreshBookingsTable(
                    selectedDateTime.toLocalDate(),
                    selectedDateTime.toLocalDate().plusDays(1)
                );
            }
        });
        
        resetButton.addActionListener(e -> {
            dateRangeSelector.reset();
            refreshBookingsTable(null, null);
        });
        
        datePanel.add(new JLabel("Select Date Range:"));
        datePanel.add(dateRangeSelector);
        datePanel.add(filterButton);
        datePanel.add(resetButton);
        
        topPanel.add(datePanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);
    }
    
    private void createMainContent() {
        // Create table with columns
        String[] columns = {
            "Booking ID", "Customer", "Musical", "Show Date/Time", 
            "Seats", "Total Price", "Booking Date"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        bookingsTable = new JTable(tableModel);
        bookingsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        // Set column widths
        bookingsTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        bookingsTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        bookingsTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        bookingsTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        bookingsTable.getColumnModel().getColumn(4).setPreferredWidth(200);
        bookingsTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        bookingsTable.getColumnModel().getColumn(6).setPreferredWidth(150);
        
        JScrollPane scrollPane = new JScrollPane(bookingsTable);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void refreshBookingsTable(LocalDate startDate, LocalDate endDate) {
        tableModel.setRowCount(0);
        try (Connection conn = DriverManager.getConnection(MusicalDataHandler.DB_URL, MusicalDataHandler.USER, MusicalDataHandler.PASS)) {
            
            StringBuilder queryBuilder = new StringBuilder(
                "SELECT r.id, c.username, m.name, r.show_date, " +
                "GROUP_CONCAT(CONCAT(s.name, bs.seat_number) SEPARATOR ', ') as seats, " +
                "r.total_price, r.created_at " +
                "FROM receipts r " +
                "JOIN customers c ON r.customer_id = c.id " +
                "JOIN musicals m ON r.musical_id = m.id " +
                "JOIN booked_seats bs ON r.id = bs.booking_id " +
                "JOIN sections s ON bs.section_id = s.id "
            );
                    
            
            if (startDate != null && endDate != null) {
                queryBuilder.append("WHERE r.show_date BETWEEN ? AND ? ");
            }
            
            queryBuilder.append("GROUP BY r.id ORDER BY r.created_at DESC");
            
            PreparedStatement pstmt = conn.prepareStatement(queryBuilder.toString());
            
            if (startDate != null && endDate != null) {
                pstmt.setDate(1, java.sql.Date.valueOf(startDate));
                pstmt.setDate(2, java.sql.Date.valueOf(endDate));
            }
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("name"),
                    rs.getTimestamp("show_date").toLocalDateTime()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    rs.getString("seats"),
                    String.format("£%.2f", rs.getDouble("total_price")),
                    rs.getTimestamp("created_at").toLocalDateTime()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error loading booking data: " + e.getMessage());
        }
    }
}

