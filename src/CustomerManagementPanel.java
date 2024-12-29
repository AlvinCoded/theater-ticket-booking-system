import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerManagementPanel extends JPanel {
    private final MusicalDataHandler dataHandler;
    private JTable customerTable;
    private DefaultTableModel tableModel;

    public CustomerManagementPanel(MusicalDataHandler dataHandler) {
        this.dataHandler = dataHandler;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create components
        createTopPanel();
        createCustomerTable();
        createBottomPanel();
        
        // Load initial data
        refreshCustomerTable();
    }

    private void createTopPanel() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addCustomerButton = new JButton("Add New Customer");
        addCustomerButton.addActionListener(e -> showAddCustomerDialog());
        topPanel.add(addCustomerButton);
        add(topPanel, BorderLayout.NORTH);
    }

    private void createCustomerTable() {
        String[] columns = {"ID", "Username", "Email", "Phone", "Created At", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Only actions column is editable
            }
        };
        
        customerTable = new JTable(tableModel);
        customerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Add action column with dropdown
        javax.swing.table.TableColumn actionColumn = customerTable.getColumnModel().getColumn(5);
        actionColumn.setCellRenderer(new ButtonRenderer());
        actionColumn.setCellEditor(new ButtonEditor(new JCheckBox()));
        
        JScrollPane scrollPane = new JScrollPane(customerTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshCustomerTable());
        bottomPanel.add(refreshButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void showAddCustomerDialog() {
        JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Add New Customer", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Create form fields
        JTextField usernameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JTextField emailField = new JTextField(20);
        JTextField phoneField = new JTextField(20);

        // Add components
        addFormField(dialog, gbc, "Username:", usernameField, 0);
        addFormField(dialog, gbc, "Password:", passwordField, 1);
        addFormField(dialog, gbc, "Email:", emailField, 2);
        addFormField(dialog, gbc, "Phone:", phoneField, 3);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();

            // Validate input
            if (validateCustomerInput(username, password, email, phone)) {
                if (dataHandler.registerCustomer(username, password, email, phone)) {
                    JOptionPane.showMessageDialog(dialog, "Customer added successfully!");
                    dialog.dispose();
                    refreshCustomerTable();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to add customer.");
                }
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        dialog.add(saveButton, gbc);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showEditCustomerDialog(int customerId, String username, String email, String phone) {
        JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Edit Customer", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Create form fields
        JTextField usernameField = new JTextField(username, 20);
        JPasswordField passwordField = new JPasswordField(20);
        JTextField emailField = new JTextField(email, 20);
        JTextField phoneField = new JTextField(phone, 20);

        // Add components
        addFormField(dialog, gbc, "Username:", usernameField, 0);
        addFormField(dialog, gbc, "New Password:", passwordField, 1);
        addFormField(dialog, gbc, "Email:", emailField, 2);
        addFormField(dialog, gbc, "Phone:", phoneField, 3);

        // Hint for password field
        passwordField.setToolTipText("Leave blank to keep current password");

        JButton updateButton = new JButton("Update");
        updateButton.addActionListener(e -> {
            String newUsername = usernameField.getText().trim();
            String newPassword = new String(passwordField.getPassword());
            String newEmail = emailField.getText().trim();
            String newPhone = phoneField.getText().trim();

            // Validate input
            if (validateCustomerUpdate(newUsername, newEmail, newPhone)) {
                if (dataHandler.updateCustomer(customerId, newUsername, newPassword, newEmail, newPhone)) {
                    JOptionPane.showMessageDialog(dialog, "Customer updated successfully!");
                    dialog.dispose();
                    refreshCustomerTable();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to update customer.");
                }
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        dialog.add(updateButton, gbc);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void refreshCustomerTable() {
        tableModel.setRowCount(0);
        List<MusicalDataHandler.CustomerInfo> customers = dataHandler.getAllCustomers();
        for (MusicalDataHandler.CustomerInfo customer : customers) {
            tableModel.addRow(new Object[]{
                customer.getId(),
                customer.getUsername(),
                customer.getEmail(),
                customer.getPhoneNumber(),
                customer.getCreatedAt(),
                "Actions"
            });
        }
    }

    private boolean validateCustomerInput(String username, String password, String email, String phone) {
        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields must be filled.");
            return false;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            JOptionPane.showMessageDialog(this, "Invalid email format.");
            return false;
        }

        if (!phone.matches("^\\d{10}$")) {
            JOptionPane.showMessageDialog(this, "Phone number must be 10 digits.");
            return false;
        }

        return true;
    }

    private boolean validateCustomerUpdate(String username, String email, String phone) {
        if (username.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username, email, and phone cannot be empty.");
            return false;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            JOptionPane.showMessageDialog(this, "Invalid email format.");
            return false;
        }

        if (!phone.matches("^\\d{10}$")) {
            JOptionPane.showMessageDialog(this, "Phone number must be 10 digits.");
            return false;
        }

        return true;
    }

    private void addFormField(JDialog dialog, GridBagConstraints gbc, 
                               String label, JComponent field, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        dialog.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        dialog.add(field, gbc);
    }

    // Custom button renderer and editor for action column
    private class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, 
                                                       int row, int column) {
            setText("Actions");
            return this;
        }
    }

    private class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private int selectedRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton("Actions");
            button.setOpaque(true);
            button.addActionListener(e -> {
                // Create popup menu
                JPopupMenu popupMenu = new JPopupMenu();
                JMenuItem editItem = new JMenuItem("Edit");
                JMenuItem deleteItem = new JMenuItem("Delete");

                editItem.addActionListener(evt -> {
                    int customerId = (int) customerTable.getValueAt(selectedRow, 0);
                    String username = (String) customerTable.getValueAt(selectedRow, 1);
                    String email = (String) customerTable.getValueAt(selectedRow, 2);
                    String phone = (String) customerTable.getValueAt(selectedRow, 3);
                    showEditCustomerDialog(customerId, username, email, phone);
                });

                deleteItem.addActionListener(evt -> {
                    int customerId = (int) customerTable.getValueAt(selectedRow, 0);
                    String username = (String) customerTable.getValueAt(selectedRow, 1);
                    
                    int confirm = JOptionPane.showConfirmDialog(
                        CustomerManagementPanel.this,
                        "Are you sure you want to delete customer '" + username + "'?",
                        "Confirm Deletion",
                        JOptionPane.YES_NO_OPTION
                    );
                    
                    if (confirm == JOptionPane.YES_OPTION) {
                        if (dataHandler.deleteCustomer(customerId)) {
                            JOptionPane.showMessageDialog(
                                CustomerManagementPanel.this, 
                                "Customer deleted successfully!"
                            );
                            refreshCustomerTable();
                        } else {
                            JOptionPane.showMessageDialog(
                                CustomerManagementPanel.this, 
                                "Failed to delete customer."
                            );
                        }
                    }
                });

                popupMenu.add(editItem);
                popupMenu.add(deleteItem);
                popupMenu.show(button, 0, button.getHeight());
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            selectedRow = row;
            return button;
        }
    }
}
