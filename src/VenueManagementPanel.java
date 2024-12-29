import java.sql.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;
//import javax.swing.table.DefaultTableModel;
//import javax.swing.table.TableColumn;


public class VenueManagementPanel extends JPanel {
    private final MusicalDataHandler dataHandler;
    private JTable venueTable;
    private DefaultTableModel tableModel;
    private JPanel seatLayoutPanel;
    
    public VenueManagementPanel(MusicalDataHandler dataHandler) {
        this.dataHandler = dataHandler;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        createTopPanel();
        createMainContent();
        refreshVenueTable();
    }
    
    private void createTopPanel() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addVenueButton = new JButton("Add New Venue");
        JButton editVenueButton = new JButton("Edit Venue");
        JButton manageSectionsButton = new JButton("Manage Sections");
        
        addVenueButton.addActionListener(e -> showAddVenueDialog());
        editVenueButton.addActionListener(e -> showEditVenueDialog());
        manageSectionsButton.addActionListener(e -> showSectionManagementDialog());
        
        topPanel.add(addVenueButton);
        topPanel.add(editVenueButton);
        topPanel.add(manageSectionsButton);
        add(topPanel, BorderLayout.NORTH);
    }
    
    private void createMainContent() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        // Venue table
        String[] columns = {"ID", "Name", "Total Capacity", "Sections"};
        tableModel = new DefaultTableModel(columns, 0);
        venueTable = new JTable(tableModel);
        venueTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Seat layout visualization with scroll capability
        seatLayoutPanel = new JPanel();
        seatLayoutPanel.setLayout(new BorderLayout());
        JPanel layoutContent = new JPanel();
        layoutContent.setLayout(new BoxLayout(layoutContent, BoxLayout.Y_AXIS));

        JScrollPane layoutScroll = new JScrollPane(layoutContent);
        layoutScroll.setBorder(BorderFactory.createTitledBorder("Seating Layout"));
        layoutScroll.getVerticalScrollBar().setUnitIncrement(16);
        seatLayoutPanel.add(layoutScroll, BorderLayout.CENTER);

        venueTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateSeatLayout(layoutContent);
            }
        });

        splitPane.setLeftComponent(new JScrollPane(venueTable));
        splitPane.setRightComponent(seatLayoutPanel);
        splitPane.setDividerLocation(400);

        add(splitPane, BorderLayout.CENTER);
    }

    private void updateSeatLayout(JPanel layoutContent) {
        int selectedRow = venueTable.getSelectedRow();
        if (selectedRow == -1) return;

        int venueId = (int)venueTable.getValueAt(selectedRow, 0);
        layoutContent.removeAll();

        // Get sections and create visual representation
        List<MusicalDataHandler.VenueSection> sections = dataHandler.getVenueSections(venueId);

        for (MusicalDataHandler.VenueSection section : sections) {
            JPanel sectionPanel = createSectionPanel(section);
            sectionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, sectionPanel.getPreferredSize().height));
            layoutContent.add(sectionPanel);
            layoutContent.add(Box.createVerticalStrut(10));
        }

        layoutContent.revalidate();
        layoutContent.repaint();
    }

    
    private void showAddVenueDialog() {
        JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Add New Venue", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JTextField nameField = new JTextField(20);
        JSpinner capacitySpinner = new JSpinner(new SpinnerNumberModel(100, 1, 10000, 1));
        
        addFormField(dialog, gbc, "Venue Name:", nameField, 0);
        addFormField(dialog, gbc, "Total Capacity:", capacitySpinner, 1);
        
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            int capacity = (Integer)capacitySpinner.getValue();
            
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter a venue name");
                return;
            }
            
            if (dataHandler.addVenue(name, capacity)) {
                JOptionPane.showMessageDialog(dialog, "Venue added successfully!");
                dialog.dispose();
                refreshVenueTable();
            }
        });
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        dialog.add(saveButton, gbc);
        
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private void showSectionManagementDialog() {
    int selectedRow = venueTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Please select a venue first");
        return;
    }

    int venueId = (int)venueTable.getValueAt(selectedRow, 0);
    String venueName = (String)venueTable.getValueAt(selectedRow, 1);
    int totalCapacity = (int)venueTable.getValueAt(selectedRow, 2);

    JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), 
        "Manage Sections - " + venueName, true);
    dialog.setLayout(new BorderLayout(10, 10));

    // Section table
    String[] columns = {"ID", "Name", "Capacity", "Base Price", "Action"};
    DefaultTableModel sectionModel = new DefaultTableModel(columns, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 4; // Only allow editing of the Action column
        }
    };
    JTable sectionTable = new JTable(sectionModel);

    // Add section panel with proper layout
    JPanel addPanel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    JTextField sectionNameField = new JTextField(15);
    JSpinner capacitySpinner = new JSpinner(new SpinnerNumberModel(50, 1, totalCapacity, 1));
    JSpinner priceSpinner = new JSpinner(new SpinnerNumberModel(50.0, 10.0, 1000.0, 5.0));

    // Calculate initial remaining capacity
    int remainingCapacity = calculateRemainingCapacity(venueId, totalCapacity);
    JLabel remainingCapacityLabel = new JLabel("Remaining Capacity: " + remainingCapacity);

    // Add components with proper constraints
    gbc.gridx = 0; gbc.gridy = 0;
    addPanel.add(new JLabel("Section Name:"), gbc);
    gbc.gridx = 1;
    addPanel.add(sectionNameField, gbc);

    gbc.gridx = 0; gbc.gridy = 1;
    addPanel.add(new JLabel("Capacity:"), gbc);
    gbc.gridx = 1;
    addPanel.add(capacitySpinner, gbc);

    gbc.gridx = 0; gbc.gridy = 2;
    addPanel.add(new JLabel("Base Price:"), gbc);
    gbc.gridx = 1;
    addPanel.add(priceSpinner, gbc);

    gbc.gridx = 0; gbc.gridy = 3;
    gbc.gridwidth = 2;
    addPanel.add(remainingCapacityLabel, gbc);

    JButton addButton = new JButton("Add Section");
    addButton.addActionListener(e -> {
        String name = sectionNameField.getText().trim();
        int capacity = (Integer)capacitySpinner.getValue();
        double price = (Double)priceSpinner.getValue();

        // Validate input
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Please enter a section name");
            return;
        }

        // Check total capacity before adding
        int currentUsedCapacity = calculateUsedCapacity(venueId);
        if (currentUsedCapacity + capacity > totalCapacity) {
            JOptionPane.showMessageDialog(dialog, 
                "Section capacity exceeds remaining venue capacity (" + 
                (totalCapacity - currentUsedCapacity) + ")");
            return;
        }

        if (dataHandler.addSection(venueId, name, capacity, price)) {
            refreshSectionTable(sectionModel, venueId);
            sectionNameField.setText("");
            int newRemaining = totalCapacity - (currentUsedCapacity + capacity);
            remainingCapacityLabel.setText("Remaining Capacity: " + newRemaining);
            
            // Update capacity spinner maximum
            ((SpinnerNumberModel)capacitySpinner.getModel()).setMaximum(newRemaining);
        }
    });

    gbc.gridx = 0; gbc.gridy = 4;
    gbc.gridwidth = 2;
    addPanel.add(addButton, gbc);

    // Add delete functionality
    TableColumn deleteColumn = sectionTable.getColumnModel().getColumn(4);
    deleteColumn.setCellRenderer(new ButtonRenderer());
    deleteColumn.setCellEditor(new ButtonEditor(new JCheckBox(), sectionTable, dataHandler, remainingCapacityLabel, capacitySpinner, totalCapacity));

    dialog.add(new JScrollPane(sectionTable), BorderLayout.CENTER);
    dialog.add(addPanel, BorderLayout.SOUTH);

    refreshSectionTable(sectionModel, venueId);
    dialog.setSize(600, 400);
    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);
}

private int calculateUsedCapacity(int venueId) {
    try (Connection conn = DriverManager.getConnection(
            dataHandler.DB_URL, dataHandler.USER, dataHandler.PASS)) {
        String query = "SELECT COALESCE(SUM(capacity), 0) as total FROM sections WHERE venue_id = ?";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setInt(1, venueId);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            return rs.getInt("total");
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return 0;
}


private int calculateRemainingCapacity(int venueId, int totalCapacity) {
    int usedCapacity = 0;
    try (Connection conn = DriverManager.getConnection(
            dataHandler.DB_URL, dataHandler.USER, dataHandler.PASS)) {
        String query = "SELECT SUM(capacity) as total FROM sections WHERE venue_id = ?";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setInt(1, venueId);
        ResultSet rs = pstmt.executeQuery();
        
        if (rs.next()) {
            usedCapacity = rs.getInt("total");
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return totalCapacity - usedCapacity;
}

    
    private void updateSeatLayout() {
        int selectedRow = venueTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        int venueId = (int)venueTable.getValueAt(selectedRow, 0);
        seatLayoutPanel.removeAll();
        seatLayoutPanel.setLayout(new GridBagLayout());
        
        // Get sections and create visual representation
        List<MusicalDataHandler.VenueSection> sections = dataHandler.getVenueSections(venueId);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        int row = 0;
        for (MusicalDataHandler.VenueSection section : sections) {
            JPanel sectionPanel = createSectionPanel(section);
            gbc.gridx = 0;
            gbc.gridy = row++;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            seatLayoutPanel.add(sectionPanel, gbc);
        }
        
        seatLayoutPanel.revalidate();
        seatLayoutPanel.repaint();
    }
    
    private JPanel createSectionPanel(MusicalDataHandler.VenueSection section) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(section.getName()));
        
        // Create visual representation of seats
        JPanel seatsPanel = new JPanel(new GridLayout(0, 10, 2, 2));
        for (int i = 0; i < section.getCapacity(); i++) {
            JLabel seat = new JLabel("■");
            seat.setForeground(Color.BLUE);
            seat.setHorizontalAlignment(SwingConstants.CENTER);
            seatsPanel.add(seat);
        }
        
        JLabel infoLabel = new JLabel(String.format("Capacity: %d | Base Price: £%.2f", 
            section.getCapacity(), section.getBasePrice()));
        
        panel.add(seatsPanel, BorderLayout.CENTER);
        panel.add(infoLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void refreshVenueTable() {
        tableModel.setRowCount(0);
        try (Connection conn = DriverManager.getConnection(
                dataHandler.DB_URL, dataHandler.USER, dataHandler.PASS)) {
            String query = "SELECT v.*, " +
                          "(SELECT COUNT(*) FROM sections WHERE venue_id = v.id) as section_count " +
                          "FROM venues v";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("total_capacity"),
                    rs.getInt("section_count")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showEditVenueDialog() {
        int selectedRow = venueTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a venue to edit");
            return;
        }

        int venueId = (int)venueTable.getValueAt(selectedRow, 0);
        String venueName = (String)venueTable.getValueAt(selectedRow, 1);
        int capacity = (int)venueTable.getValueAt(selectedRow, 2);

        JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), 
            "Edit Venue", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(venueName, 20);
        JSpinner capacitySpinner = new JSpinner(new SpinnerNumberModel(capacity, 1, 10000, 1));

        addFormField(dialog, gbc, "Venue Name:", nameField, 0);
        addFormField(dialog, gbc, "Total Capacity:", capacitySpinner, 1);

        JButton updateButton = new JButton("Update");
        updateButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            int newCapacity = (Integer)capacitySpinner.getValue();

            if (dataHandler.updateVenue(venueId, name, newCapacity)) {
                JOptionPane.showMessageDialog(dialog, "Venue updated successfully!");
                dialog.dispose();
                refreshVenueTable();
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        dialog.add(updateButton, gbc);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void addFormField(Container container, GridBagConstraints gbc, String label, JComponent field, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        container.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        container.add(field, gbc);
    }


    private void refreshSectionTable(DefaultTableModel model, int venueId) {
        model.setRowCount(0);
        int totalSectionCapacity = 0;

        try (Connection conn = DriverManager.getConnection(
                dataHandler.DB_URL, dataHandler.USER, dataHandler.PASS)) {
            String query = "SELECT * FROM sections WHERE venue_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, venueId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("capacity"),
                    rs.getDouble("base_price")
                });
                totalSectionCapacity += rs.getInt("capacity");
            }

            // Update the sections count in venues table
            String updateQuery = "UPDATE venues SET section_count = ? WHERE id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
            updateStmt.setInt(1, model.getRowCount());
            updateStmt.setInt(2, venueId);
            updateStmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        refreshVenueTable();
    }

class ButtonRenderer extends JButton implements TableCellRenderer {
    public ButtonRenderer() {
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        setText("Delete");
        return this;
    }
}

class ButtonEditor extends DefaultCellEditor {
    protected JButton button;
    private String label;
    private boolean isPushed;
    private JTable table;
    private MusicalDataHandler dataHandler;
    private JLabel remainingCapacityLabel;
    private JSpinner capacitySpinner;
    private int totalCapacity;
    private int venueId;

    public ButtonEditor(JCheckBox checkBox, JTable table, MusicalDataHandler dataHandler, 
                       JLabel remainingCapacityLabel, JSpinner capacitySpinner, int totalCapacity) {
        super(checkBox);
        this.table = table;
        this.dataHandler = dataHandler;
        this.remainingCapacityLabel = remainingCapacityLabel;
        this.totalCapacity = totalCapacity;
        
        button = new JButton();
        button.setOpaque(true);
        button.addActionListener(e -> {
            fireEditingStopped();
            int row = table.getSelectedRow();
            if (row != -1) {
                int sectionId = (int) table.getValueAt(row, 0);
                int sectionCapacity = (int) table.getValueAt(row, 2);
                
                int confirm = JOptionPane.showConfirmDialog(
                    table,
                    "Are you sure you want to delete this section?",
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );
                
                if (confirm == JOptionPane.YES_OPTION) {
                    if (dataHandler.deleteSection(sectionId)) {
                        DefaultTableModel model = (DefaultTableModel) table.getModel();
                        model.removeRow(row);
                        
                        // Update remaining capacity immediately
                        int newRemaining = calculateRemainingCapacity(venueId, totalCapacity);
                        remainingCapacityLabel.setText("Remaining Capacity: " + newRemaining);
                        ((SpinnerNumberModel)capacitySpinner.getModel()).setMaximum(newRemaining);
                    }
                }
            }
        });
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                               boolean isSelected, int row, int column) {
        label = (value == null) ? "Delete" : value.toString();
        button.setText(label);
        isPushed = true;
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        isPushed = false;
        return label;
    }

    @Override
    public boolean stopCellEditing() {
        isPushed = false;
        return super.stopCellEditing();
    }
}


}

