import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class AddressBook extends JFrame {
    private List<Contact> contacts;
    private JTable contactTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JLabel statusLabel;
    private JToggleButton showStationaryColumn;
    private static final String SAVE_FILE = "address_book.dat";

    public AddressBook() {
        contacts = new ArrayList<>();
        setTitle("Address Book");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Меню
        JMenuBar menuBar = getjMenuBar();
        setJMenuBar(menuBar);

        // Тулбар
        JToolBar toolBar = new JToolBar();
        JButton addButton = new JButton("Add");
        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");
        JButton resetButton = new JButton("Reset Book");
        showStationaryColumn = new JToggleButton("Hide Stationary", true);
        toolBar.add(addButton);
        toolBar.add(editButton);
        toolBar.add(deleteButton);
        toolBar.add(resetButton);
        toolBar.addSeparator();
        toolBar.add(showStationaryColumn);

        // Таблця
        String[] columnNames = {"Name", "Phone", "Email", "Stationary"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        contactTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(contactTable);

        // Пошук
        JPanel searchPanel = new JPanel();
        searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        searchPanel.add(new JLabel("Search: "));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // рядок стану
        statusLabel = new JLabel("Ready");

        // Розміщення компонентів
        setLayout(new BorderLayout());
        add(toolBar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Додаємо панель пошуку та статусний рядок в нижню частину
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(searchPanel, BorderLayout.NORTH);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);

        // контекстне меню
        JPopupMenu contextMenu = new JPopupMenu();
        JMenuItem editMenuItem = new JMenuItem("Edit");
        JMenuItem deleteMenuItem = new JMenuItem("Delete");
        contextMenu.add(editMenuItem);
        contextMenu.add(deleteMenuItem);

        contactTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                handleContextMenu(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                handleContextMenu(e);
            }

            private void handleContextMenu(MouseEvent e) {
                int r = contactTable.rowAtPoint(e.getPoint());
                if (r >= 0 && r < contactTable.getRowCount()) {
                    contactTable.setRowSelectionInterval(r, r);
                } else {
                    contactTable.clearSelection();
                }

                int rowindex = contactTable.getSelectedRow();
                if (rowindex < 0)
                    return;
                if (e.isPopupTrigger() && e.getComponent() instanceof JTable) {
                    contextMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        // лістенери
        addButton.addActionListener(e -> showAddDialog());
        editButton.addActionListener(e -> showEditDialog());
        deleteButton.addActionListener(e -> deleteContact());
        resetButton.addActionListener(e -> resetAddressBook());
        searchButton.addActionListener(e -> searchContacts());
        editMenuItem.addActionListener(e -> showEditDialog());
        deleteMenuItem.addActionListener(e -> deleteContact());
        showStationaryColumn.addActionListener(e -> toggleStationaryColumn());

        // лістенер для поля пошуку для пошуку в реальному часі
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                searchContacts();
            }
        });

        // завантаження контактів
        loadContacts();

        // колонка "Stationary" за замовчуванням показується
        toggleStationaryColumn();
    }

    private JMenuBar getjMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem loadItem = new JMenuItem("Load");
        JMenuItem exitItem = new JMenuItem("Exit");
        saveItem.addActionListener(e -> saveContacts());
        loadItem.addActionListener(e -> loadContacts());
        exitItem.addActionListener(e -> {
            saveContacts();
            System.exit(0);
        });
        fileMenu.add(saveItem);
        fileMenu.add(loadItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        return menuBar;
    }

    private void toggleStationaryColumn() {
        TableColumn stationaryColumn = contactTable.getColumnModel().getColumn(3);
        if (showStationaryColumn.isSelected()) {
            // показати колонку
            stationaryColumn.setMinWidth(75);
            stationaryColumn.setMaxWidth(Short.MAX_VALUE);
            showStationaryColumn.setText("Hide Stationary");

            // Розподіляємо ширину рівномірно між усіма стовпчиками
            int tableWidth = contactTable.getWidth();
            int columnCount = contactTable.getColumnCount();
            int columnWidth = tableWidth / columnCount;

            for (int i = 0; i < columnCount; i++) {
                TableColumn column = contactTable.getColumnModel().getColumn(i);
                column.setPreferredWidth(columnWidth);
            }
        } else {
            // приховати
            stationaryColumn.setMinWidth(0);
            stationaryColumn.setMaxWidth(0);
            stationaryColumn.setWidth(0);
            showStationaryColumn.setText("Show Stationary");

            // Розподіляємо ширину рівномірно між видимими стовпчиками
            int tableWidth = contactTable.getWidth();
            int visibleColumnCount = contactTable.getColumnCount() - 1; // видаляємо колонку що сховали
            int columnWidth = tableWidth / visibleColumnCount;

            for (int i = 0; i < contactTable.getColumnCount(); i++) {
                if (i != 3) {
                    TableColumn column = contactTable.getColumnModel().getColumn(i);
                    column.setPreferredWidth(columnWidth);
                }
            }
        }
        contactTable.revalidate();
        contactTable.repaint();
    }

    private void showAddDialog() {
        JDialog addDialog = new JDialog(this, "Add Contact", true);
        addDialog.setLayout(new GridLayout(5, 2));

        JTextField nameField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField emailField = new JTextField();
        JCheckBox isStationaryField = new JCheckBox();

        addDialog.add(new JLabel("Name:"));
        addDialog.add(nameField);
        addDialog.add(new JLabel("Phone:"));
        addDialog.add(phoneField);
        addDialog.add(new JLabel("Email:"));
        addDialog.add(emailField);
        addDialog.add(new JLabel("Stationary:"));
        addDialog.add(isStationaryField);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            String name = nameField.getText();
            String phone = phoneField.getText();
            String email = emailField.getText();
            boolean isStationary = isStationaryField.isSelected();

            if (validateInput(name, phone, email)) {
                Contact contact = new Contact(name, phone, email, isStationary);
                contacts.add(contact);
                tableModel.addRow(new Object[]{name, phone, email, isStationary});
                addDialog.dispose();
                statusLabel.setText("Contact added");
            }
        });

        addDialog.add(saveButton);
        addDialog.add(new JButton("Cancel") {{ addActionListener(e -> addDialog.dispose()); }});

        addDialog.pack();
        addDialog.setLocationRelativeTo(this);
        addDialog.setVisible(true);
    }

    private void showEditDialog() {
        int selectedRow = contactTable.getSelectedRow();
        if (selectedRow != -1) {
            Contact contact = contacts.get(selectedRow);
            JDialog editDialog = new JDialog(this, "Edit Contact", true);
            editDialog.setLayout(new GridLayout(5, 2));

            JTextField nameField = new JTextField(contact.getName());
            JTextField phoneField = new JTextField(contact.getPhone());
            JTextField emailField = new JTextField(contact.getEmail());
            JCheckBox isStationaryField = new JCheckBox("", contact.isStationary());

            editDialog.add(new JLabel("Name:"));
            editDialog.add(nameField);
            editDialog.add(new JLabel("Phone:"));
            editDialog.add(phoneField);
            editDialog.add(new JLabel("Email:"));
            editDialog.add(emailField);
            editDialog.add(new JLabel("Stationary:"));
            editDialog.add(isStationaryField);

            JButton saveButton = new JButton("Save");
            saveButton.addActionListener(e -> {
                String name = nameField.getText();
                String phone = phoneField.getText();
                String email = emailField.getText();
                boolean isStationary = isStationaryField.isSelected();

                if (validateInput(name, phone, email)) {
                    contact.setName(name);
                    contact.setPhone(phone);
                    contact.setEmail(email);
                    contact.setStationary(isStationary);

                    tableModel.setValueAt(name, selectedRow, 0);
                    tableModel.setValueAt(phone, selectedRow, 1);
                    tableModel.setValueAt(email, selectedRow, 2);
                    tableModel.setValueAt(isStationary, selectedRow, 3);

                    editDialog.dispose();
                    statusLabel.setText("Contact updated");
                }
            });

            editDialog.add(saveButton);
            editDialog.add(new JButton("Cancel") {{ addActionListener(e -> editDialog.dispose()); }});

            editDialog.pack();
            editDialog.setLocationRelativeTo(this);
            editDialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a contact to edit", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean validateInput(String name, String phone, String email) {
        if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!Pattern.matches("^[\\d\\s+()-]{10,20}$", phone)) {
            JOptionPane.showMessageDialog(this, "Invalid phone number", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!Pattern.matches("^[A-Za-z0-9+_.-]+@(.+)$", email)) {
            JOptionPane.showMessageDialog(this, "Invalid email address", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    private void deleteContact() {
        int selectedRow = contactTable.getSelectedRow();
        if (selectedRow != -1) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to delete this contact?",
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                contacts.remove(selectedRow);
                tableModel.removeRow(selectedRow);
                statusLabel.setText("Contact deleted");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a contact to delete", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetAddressBook() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to reset the entire address book? This action cannot be undone.",
                "Confirm Reset",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            contacts.clear();
            tableModel.setRowCount(0);
            statusLabel.setText("Address book reset");
        }
    }

    private void searchContacts() {
        String searchTerm = searchField.getText().toLowerCase();
        tableModel.setRowCount(0);
        for (Contact contact : contacts) {
            if (contact.getName().toLowerCase().contains(searchTerm) ||
                    contact.getPhone().toLowerCase().contains(searchTerm) ||
                    contact.getEmail().toLowerCase().contains(searchTerm)) {
                tableModel.addRow(new Object[]{contact.getName(), contact.getPhone(), contact.getEmail(), contact.isStationary()});
            }
        }
        statusLabel.setText("Search completed: " + tableModel.getRowCount() + " results found");
    }

    private void saveContacts() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            oos.writeObject(contacts);
            statusLabel.setText("Contacts saved successfully");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving contacts: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadContacts() {
        File file = new File(SAVE_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                contacts = (List<Contact>) ois.readObject();
                updateTable();
                statusLabel.setText("Contacts loaded successfully");
            } catch (IOException | ClassNotFoundException e) {
                JOptionPane.showMessageDialog(this, "Error loading contacts: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateTable() {
        tableModel.setRowCount(0);
        for (Contact contact : contacts) {
            tableModel.addRow(new Object[]{contact.getName(), contact.getPhone(), contact.getEmail(), contact.isStationary()});
        }
        toggleStationaryColumn();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AddressBook addressBook = new AddressBook();
            addressBook.setVisible(true);
        });
    }
}

class Contact implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private String phone;
    private String email;
    private boolean isStationary;

    public Contact(String name, String phone, String email, boolean isStationary) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.isStationary = isStationary;
    }

    // гетери і сетери
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public boolean isStationary() { return isStationary; }
    public void setStationary(boolean stationary) { isStationary = stationary; }
}