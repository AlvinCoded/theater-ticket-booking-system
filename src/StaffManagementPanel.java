import java.sql.*;
import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class StaffManagementPanel extends JPanel {
    private final MusicalDataHandler dataHandler;
    private JTable staffTable;
    private DefaultTableModel tableModel;

    public StaffManagementPanel(MusicalDataHandler dataHandler) {
        this.dataHandler = dataHandler;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create components
        createTopPanel();
        createStaffTable();
        createBottomPanel();
        
        // Load initial data
        refreshStaffTable();
    }

    private void createTopPanel() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addStaffButton = new JButton("Add New Staff");
        addStaffButton.addActionListener(e -> showAddStaffDialog());
        topPanel.add(addStaffButton);
        add(topPanel, BorderLayout.NORTH);
    }

    private void createStaffTable() {
        String[] columns = {"ID", "Username", "Role", "Permissions", "Created At"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        staffTable = new JTable(tableModel);
        staffTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(staffTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton editButton = new JButton("Edit Selected");
        JButton deleteButton = new JButton("Delete Selected");
        
        editButton.addActionListener(e -> editSelectedStaff());
        deleteButton.addActionListener(e -> deleteSelectedStaff());
        
        bottomPanel.add(editButton);
        bottomPanel.add(deleteButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void showAddStaffDialog() {
        JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Add New Staff", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Create form fields
        JTextField usernameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"STAFF", "ADMIN"});
        JCheckBox[] permissionBoxes = {
            new JCheckBox("View Bookings"),
            new JCheckBox("Manage Musicals"),
            new JCheckBox("Generate Reports")
        };

        // Add components
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        dialog.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        dialog.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1;
        dialog.add(roleCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        dialog.add(new JLabel("Permissions:"), gbc);
        JPanel permissionsPanel = new JPanel(new GridLayout(0, 1));
        for (JCheckBox box : permissionBoxes) {
            permissionsPanel.add(box);
        }
        gbc.gridx = 1;
        dialog.add(permissionsPanel, gbc);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String role = (String)roleCombo.getSelectedItem();
            
            // Collect permissions
            List<String> selectedPermissions = new ArrayList<>();
            for (JCheckBox box : permissionBoxes) {
                if (box.isSelected()) {
                    selectedPermissions.add(box.getText());
                }
            }
            String permissions = String.join(",", selectedPermissions);

            if (dataHandler.createStaffMember(username, password, role, permissions)) {
                JOptionPane.showMessageDialog(dialog, "Staff member added successfully!");
                dialog.dispose();
                refreshStaffTable();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to add staff member.");
            }
        });

        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        dialog.add(saveButton, gbc);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void refreshStaffTable() {
        tableModel.setRowCount(0);
        for (MusicalDataHandler.StaffMember staff : dataHandler.getAllStaff()) {
            tableModel.addRow(new Object[]{
                staff.getId(),
                staff.getUsername(),
                staff.getRole(),
                staff.getPermissions(),
                staff.getCreatedAt()
            });
        }
    }

    private void editSelectedStaff() {
        int selectedRow = staffTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a staff member to edit.");
            return;
        }

        int staffId = (int) staffTable.getValueAt(selectedRow, 0);
        String username = (String) staffTable.getValueAt(selectedRow, 1);
        String currentRole = (String) staffTable.getValueAt(selectedRow, 2);
        String currentPermissions = (String) staffTable.getValueAt(selectedRow, 3);

        JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Edit Staff Member", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Create form fields
        JTextField usernameField = new JTextField(username, 20);
        usernameField.setEnabled(false); // Username cannot be changed
        JPasswordField passwordField = new JPasswordField(20);
        passwordField.setToolTipText("Leave empty to keep current password");

        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"STAFF", "ADMIN"});
        roleCombo.setSelectedItem(currentRole);

        // Create permission checkboxes
        List<String> currentPermList = Arrays.asList(currentPermissions.split(","));
        JCheckBox[] permissionBoxes = {
            new JCheckBox("View Bookings", currentPermList.contains("View Bookings")),
            new JCheckBox("Manage Musicals", currentPermList.contains("Manage Musicals")),
            new JCheckBox("Generate Reports", currentPermList.contains("Generate Reports"))
        };

        // Add components
        addFormField(dialog, gbc, "Username:", usernameField, 0);
        addFormField(dialog, gbc, "New Password:", passwordField, 1);
        addFormField(dialog, gbc, "Role:", roleCombo, 2);

        JPanel permissionsPanel = new JPanel(new GridLayout(0, 1));
        for (JCheckBox box : permissionBoxes) {
            permissionsPanel.add(box);
        }

        gbc.gridx = 0; gbc.gridy = 3;
        dialog.add(new JLabel("Permissions:"), gbc);
        gbc.gridx = 1;
        dialog.add(permissionsPanel, gbc);

        JButton updateButton = new JButton("Update");
        updateButton.addActionListener(e -> {
            // Collect permissions
            List<String> selectedPermissions = new ArrayList<>();
            for (JCheckBox box : permissionBoxes) {
                if (box.isSelected()) {
                    selectedPermissions.add(SecurityUtils.sanitizeInput(box.getText()));
                }
            }
            String permissions = String.join(",", selectedPermissions);
            String password = new String(passwordField.getPassword());

            try {
                if (!password.isEmpty()) {
                    // Update with new password
                    String hashedPassword = SecurityUtils.hashPassword(password);
                    dataHandler.updateStaffMemberWithPassword(staffId, 
                        (String)roleCombo.getSelectedItem(), permissions, hashedPassword);
                } else {
                    // Update without changing password
                    dataHandler.updateStaffMember(staffId, 
                        (String)roleCombo.getSelectedItem(), permissions);
                }
                JOptionPane.showMessageDialog(dialog, "Staff member updated successfully!");
                dialog.dispose();
                refreshStaffTable();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "Error updating staff member: " + ex.getMessage());
            }
        });

        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        dialog.add(updateButton, gbc);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void deleteSelectedStaff() {
        int selectedRow = staffTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select a staff member to delete.");
            return;
        }

        int staffId = (int) staffTable.getValueAt(selectedRow, 0);
        String username = (String) staffTable.getValueAt(selectedRow, 1);

        // Prevent deleting the last admin
        if (!canDeleteStaffMember(staffId)) {
            JOptionPane.showMessageDialog(this, 
                "Cannot delete the last administrator account.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete staff member '" + username + "'?\n" +
            "This action cannot be undone.",
            "Confirm Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (dataHandler.deleteStaffMember(staffId)) {
                    JOptionPane.showMessageDialog(this, 
                        "Staff member deleted successfully!");
                    refreshStaffTable();
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Failed to delete staff member.");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error deleting staff member: " + e.getMessage());
            }
        }
    }

    private boolean canDeleteStaffMember(int staffId) {
        try {
            int adminCount = dataHandler.getAdminCount();
            String roleToDelete = dataHandler.getStaffRole(staffId);
            return !"ADMIN".equals(roleToDelete) || adminCount > 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private void addFormField(JDialog dialog, GridBagConstraints gbc, 
                         String label, JComponent field, int row) {
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.gridwidth = 1;
    dialog.add(new JLabel(label), gbc);
    
    gbc.gridx = 1;
    dialog.add(field, gbc);
}


}

