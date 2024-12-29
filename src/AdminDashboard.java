import javax.swing.*;
import java.awt.*;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.time.*;
import java.sql.*;

public class AdminDashboard extends JPanel {
    private final MusicalDataHandler dataHandler;
    private JFreeChart incomeChart;
    private ChartPanel chartPanel;
    private final String userPermissions;
    private JLabel welcomeLabel;

    
    public AdminDashboard(MusicalDataHandler dataHandler, String permissions) {
        this.dataHandler = dataHandler;
        this.userPermissions = permissions;
        setLayout(new BorderLayout());
        
        // Dashboard and Welcome message components
        createWelcomePanel();
        createDashboard();
    }
    
    private void createWelcomePanel() {
        JPanel welcomePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        welcomeLabel = new JLabel(getWelcomeMessage());
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 0));
        welcomePanel.add(welcomeLabel);
        add(welcomePanel, BorderLayout.NORTH);
    }

    private String getWelcomeMessage() {
        int hour = java.time.LocalDateTime.now().getHour();
        String timeBasedGreeting;
        if (hour < 12) {
            timeBasedGreeting = "Good Morning";
        } else if (hour < 17) {
            timeBasedGreeting = "Good Afternoon";
        } else {
            timeBasedGreeting = "Good Evening";
        }
        return timeBasedGreeting + ", Admin!";
    }
    
    private void createDashboard() {
        JPanel mainContent = new JPanel(new BorderLayout());
        
        // Create top stats panel - only visible if has "Generate Reports" permission
        if (userPermissions.contains("Generate Reports")) {
            JPanel statsPanel = createStatsPanel();
            mainContent.add(statsPanel, BorderLayout.NORTH);
            
            // Create income chart
            chartPanel = createIncomeChart();
            mainContent.add(chartPanel, BorderLayout.CENTER);
        }
        
        // Create control panel based on permissions
        JPanel controlPanel = createControlPanel();
        mainContent.add(controlPanel, BorderLayout.SOUTH);
        
        add(mainContent, BorderLayout.CENTER);
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Only show refresh button if user has View Bookings permission
        if (userPermissions.contains("View Bookings")) {
            JButton refreshBtn = new JButton("Refresh Data");
            refreshBtn.addActionListener(e -> refreshData());
            panel.add(refreshBtn);
        }
        
        // Only show export button if user has Generate Reports permission
        if (userPermissions.contains("Generate Reports")) {
            JButton exportBtn = new JButton("Export Report");
            exportBtn.addActionListener(e -> exportReport());
            panel.add(exportBtn);
        }
        
        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Show Total Income Card only if user has Generate Reports permission
        if (userPermissions.contains("Generate Reports")) {
            panel.add(createStatCard("Total Income", getTotalIncome(), "£"));
        }
        
        // Show Total Bookings Card only if user has View Bookings permission
        if (userPermissions.contains("View Bookings")) {
            panel.add(createStatCard("Total Bookings", getTotalBookings(), ""));
        }
        
        // Show Available Shows Card only if user has Manage Musicals permission
        if (userPermissions.contains("Manage Musicals")) {
            panel.add(createStatCard("Available Shows", getAvailableShows(), ""));
        }
        
        return panel;
    }
    
    private JPanel createStatCard(String title, String value, String prefix) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel titleLabel = new JLabel(title);
        JLabel valueLabel = new JLabel(prefix + value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 24));
        
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(valueLabel);
        
        return card;
    }
    
    private ChartPanel createIncomeChart() {
        TimeSeriesCollection dataset = createIncomeDataset();
        
        incomeChart = ChartFactory.createTimeSeriesChart(
            "Monthly Income",
            "Date",
            "Income (£)",
            dataset,
            true,
            true,
            false
        );
        
        XYPlot plot = incomeChart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.setRangeGridlinePaint(Color.GRAY);
        
        ChartPanel chartPanel = new ChartPanel(incomeChart);
        chartPanel.setPreferredSize(new Dimension(800, 400));
        
        return chartPanel;
    }
    
    private TimeSeriesCollection createIncomeDataset() {
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        TimeSeries series = new TimeSeries("Income");
        
        try (Connection conn = DriverManager.getConnection(
                dataHandler.DB_URL, dataHandler.USER, dataHandler.PASS)) {
            String query = "SELECT DATE(created_at) as date, SUM(amount) as total " +
                          "FROM income_data GROUP BY DATE(created_at) " +
                          "ORDER BY date";
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                series.add(
                    new Day(rs.getDate("date")),
                    rs.getDouble("total")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        dataset.addSeries(series);
        return dataset;
    }
    
    private String getTotalIncome() {
        try (Connection conn = DriverManager.getConnection(MusicalDataHandler.DB_URL, MusicalDataHandler.USER, MusicalDataHandler.PASS)) {
            String query = "SELECT SUM(amount) as total FROM income_data";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            if (rs.next()) {
                return String.format("%.2f", rs.getDouble("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "0.00";
    }
    
    private String getTotalBookings() {
        try (Connection conn = DriverManager.getConnection(MusicalDataHandler.DB_URL, MusicalDataHandler.USER, MusicalDataHandler.PASS)) {
            String query = "SELECT COUNT(*) as total FROM receipts";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            if (rs.next()) {
                return String.valueOf(rs.getInt("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "0";
    }
    
    private String getAvailableShows() {
        try (Connection conn = DriverManager.getConnection(
                MusicalDataHandler.DB_URL, MusicalDataHandler.USER, MusicalDataHandler.PASS)) {
            String query = "SELECT COUNT(*) as total FROM musicals WHERE available_tickets > 0";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            if (rs.next()) {
                return String.valueOf(rs.getInt("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "0";
    }
    
    private void refreshData() {
        // Only refresh data if user has appropriate permissions
        if (userPermissions.contains("View Bookings") || 
            userPermissions.contains("Generate Reports")) {
            TimeSeriesCollection dataset = createIncomeDataset();
            incomeChart.getXYPlot().setDataset(dataset);
            chartPanel.repaint();
        } else {
            JOptionPane.showMessageDialog(this, "You don't have permission to refresh data.");
        }
    }
    
    private void exportReport() {
        // Only allow export if user has Generate Reports permission
        if (userPermissions.contains("Generate Reports")) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Report");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                JOptionPane.showMessageDialog(this, 
                    "Report exported successfully!");
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                "You don't have permission to export reports.");
        }
    }
}
