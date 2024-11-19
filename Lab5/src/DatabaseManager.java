import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class DatabaseManager extends JFrame {
    private Connection connection;
    private JTree dbTree;
    private JTable tableView;

    public DatabaseManager() {
        setTitle("Database Manager");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        initUI();
    }

    private void initUI() {
        JMenuBar menuBar = new JMenuBar();
        JMenu dbMenu = getjMenu();
        menuBar.add(dbMenu);

        JMenu tableMenu = new JMenu("Table");
        JMenuItem insertItem = new JMenuItem("Insert");
        insertItem.addActionListener(_ -> insertRecord(null));
        tableMenu.add(insertItem);

        JMenuItem editItem = new JMenuItem("Edit");
        editItem.addActionListener(_ -> editRecord(null));
        tableMenu.add(editItem);

        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.addActionListener(_ -> deleteRecord(null));
        tableMenu.add(deleteItem);
        menuBar.add(tableMenu);

        JMenu searchMenu = new JMenu("Search");
        JMenuItem searchItem = new JMenuItem("Search");
        searchItem.addActionListener(_ -> searchRecord(null));
        searchMenu.add(searchItem);
        menuBar.add(searchMenu);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem metadataItem = new JMenuItem("Database Metadata");
        metadataItem.addActionListener(_ -> showDatabaseMetadata());
        helpMenu.add(metadataItem);

        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(_ -> showAboutDialog());
        helpMenu.add(aboutItem);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        dbTree = new JTree();
        dbTree.addTreeSelectionListener(e -> loadTableData(e.getPath()));
        JScrollPane treeScrollPane = new JScrollPane(dbTree);

        tableView = new JTable();
        JScrollPane tableScrollPane = new JScrollPane(tableView);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScrollPane, tableScrollPane);
        splitPane.setDividerLocation(300);

        add(splitPane);
        addContextMenuToTree(dbTree);
    }

    private JMenu getjMenu() {
        JMenu dbMenu = new JMenu("Database");
        JMenuItem connectItem = new JMenuItem("Connect");
        connectItem.addActionListener(_ -> connectToDatabase());
        dbMenu.add(connectItem);

        JMenuItem disconnectItem = new JMenuItem("Disconnect");
        disconnectItem.addActionListener(_ -> disconnectFromDatabase());
        dbMenu.add(disconnectItem);

        JMenuItem queryToolItem = new JMenuItem("Query Tool");
        queryToolItem.addActionListener(_ -> openQueryTool());
        dbMenu.add(queryToolItem);

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(_ -> System.exit(0));
        dbMenu.add(exitItem);
        return dbMenu;
    }

    private void connectToDatabase() {
        JPanel panel = new JPanel(new GridLayout(5, 2));

        JTextField hostField = new JTextField("localhost");
        JTextField portField = new JTextField("5432");
        JTextField dbNameField = new JTextField();
        JTextField userField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        panel.add(new JLabel("Host:"));
        panel.add(hostField);
        panel.add(new JLabel("Port:"));
        panel.add(portField);
        panel.add(new JLabel("Database Name:"));
        panel.add(dbNameField);
        panel.add(new JLabel("Username:"));
        panel.add(userField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Database Connection",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String host = hostField.getText();
            String port = portField.getText();
            String dbName = dbNameField.getText();
            String user = userField.getText();
            String password = new String(passwordField.getPassword());

            if (host.isEmpty() || port.isEmpty() || dbName.isEmpty() || user.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields must be filled!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String url = "jdbc:postgresql://" + host + ":" + port + "/" + dbName;

            try {
                connection = DriverManager.getConnection(url, user, password);
                loadDatabaseStructure();
                JOptionPane.showMessageDialog(this, "Connected successfully!");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Connection failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void disconnectFromDatabase() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                dbTree.setModel(null);
                tableView.setModel(new DefaultTableModel());
                JOptionPane.showMessageDialog(this, "Disconnected successfully!");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error during disconnection: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadDatabaseStructure() {
        try {
            DefaultMutableTreeNode root = new DefaultMutableTreeNode(connection.getCatalog());
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});

            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                DefaultMutableTreeNode tableNode = new DefaultMutableTreeNode(tableName);
                root.add(tableNode);
            }

            DefaultTreeModel treeModel = new DefaultTreeModel(root);
            dbTree.setModel(treeModel);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load database structure: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTableData(TreePath path) {
        if (path.getPathCount() < 2) return;

        String tableName = path.getLastPathComponent().toString();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName);
            setModelTableView(rs);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load table data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setModelTableView(ResultSet rs) throws SQLException {
        MetaData result = getMetaData(rs);

        tableView.setModel(new DefaultTableModel(result.data(), result.columnNames()));
    }

    private static MetaData getMetaData(ResultSet rs) throws SQLException {
        ResultSetMetaData rsMeta = rs.getMetaData();

        Vector<String> columnNames = new Vector<>();
        for (int i = 1; i <= rsMeta.getColumnCount(); i++) {
            columnNames.add(rsMeta.getColumnName(i));
        }

        Vector<Vector<Object>> data = new Vector<>();
        while (rs.next()) {
            Vector<Object> row = new Vector<>();
            for (int i = 1; i <= rsMeta.getColumnCount(); i++) {
                row.add(rs.getObject(i));
            }
            data.add(row);
        }
        return new MetaData(columnNames, data);
    }

    private record MetaData(Vector<String> columnNames, Vector<Vector<Object>> data) { }

    private void addContextMenuToTree(JTree tree) {
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                    if (path == null) return;

                    tree.setSelectionPath(path);
                    DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();

                    if (selectedNode.isLeaf()) {
                        String tableName = selectedNode.getUserObject().toString();

                        JPopupMenu contextMenu = getjPopupMenu(tableName);

                        contextMenu.show(tree, e.getX(), e.getY());
                    }
                }
            }

            private JPopupMenu getjPopupMenu(String tableName) {
                JPopupMenu contextMenu = new JPopupMenu();

                JMenuItem insertItem = new JMenuItem("Insert");
                insertItem.addActionListener(_ -> insertRecord(tableName));

                JMenuItem editItem = new JMenuItem("Edit");
                editItem.addActionListener(_ -> editRecord(tableName));

                JMenuItem deleteItem = new JMenuItem("Delete");
                deleteItem.addActionListener(_ -> deleteRecord(tableName));

                JMenuItem searchItem = new JMenuItem("Search");
                searchItem.addActionListener(_ -> searchRecord(tableName));

                contextMenu.add(insertItem);
                contextMenu.add(editItem);
                contextMenu.add(deleteItem);
                contextMenu.add(searchItem);
                return contextMenu;
            }
        });
    }

    private void openQueryTool() {
        JPanel panel = new JPanel(new BorderLayout());

        JTextArea queryArea = new JTextArea(10, 50);
        queryArea.setWrapStyleWord(true);
        queryArea.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(queryArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton executeButton = new JButton("Execute");
        panel.add(executeButton, BorderLayout.SOUTH);

        JDialog queryDialog = new JDialog(this, "Query Tool", true);
        queryDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        queryDialog.add(panel);
        queryDialog.pack();
        queryDialog.setLocationRelativeTo(this);

        executeButton.addActionListener(_ -> {
            String query = queryArea.getText().trim();
            if (query.isEmpty()) {
                JOptionPane.showMessageDialog(queryDialog, "Please enter a query.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                executeQuery(query, queryDialog);
            }
        });

        queryDialog.setVisible(true);
    }

    private void executeQuery(String query, JDialog dialog) {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            MetaData result = getMetaData(rs);

            JTable resultTable = new JTable(new DefaultTableModel(result.data(), result.columnNames()));
            JScrollPane resultScrollPane = new JScrollPane(resultTable);

            JPanel resultPanel = new JPanel(new BorderLayout());
            resultPanel.add(resultScrollPane, BorderLayout.CENTER);

            dialog.setContentPane(resultPanel);
            dialog.revalidate();
            dialog.repaint();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error executing query: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void insertRecord(String tableName) {
        if (tableName == null || tableName.trim().isEmpty()) {
            tableName = JOptionPane.showInputDialog(this, "Enter table name for insertion:", "Insert Record", JOptionPane.PLAIN_MESSAGE);
            if (tableName == null || tableName.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Table name cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        try {
            metaData resultMetaData = getMetaData(tableName);

            int result = JOptionPane.showConfirmDialog(this, resultMetaData.panel(), "Insert Record", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                StringBuilder query = new StringBuilder("INSERT INTO " + tableName + " (");
                StringBuilder values = new StringBuilder("VALUES (");

                for (Map.Entry<String, JTextField> entry : resultMetaData.fields().entrySet()) {
                    query.append(entry.getKey()).append(",");
                    values.append("'").append(entry.getValue().getText().replace("'", "''")).append("',");
                }

                query.setLength(query.length() - 1); // Видаляємо зайву кому
                values.setLength(values.length() - 1);
                query.append(") ").append(values).append(");");

                Statement statement = connection.createStatement();
                statement.executeUpdate(query.toString());
                JOptionPane.showMessageDialog(this, "Record inserted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error inserting record: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        loadDatabaseStructure();
    }

    private metaData getMetaData(String tableName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet columns = metaData.getColumns(null, null, tableName, null);

        JPanel panel = new JPanel(new GridLayout(0, 2));
        HashMap<String, JTextField> fields = new HashMap<>();

        while (columns.next()) {
            String columnName = columns.getString("COLUMN_NAME");
            panel.add(new JLabel(columnName + ":"));
            JTextField textField = new JTextField();
            panel.add(textField);
            fields.put(columnName, textField);
        }
        return new metaData(panel, fields);
    }

    private record metaData(JPanel panel, HashMap<String, JTextField> fields) { }

    private void editRecord(String tableName) {
        if (tableName == null || tableName.trim().isEmpty()) {
            tableName = JOptionPane.showInputDialog(this, "Enter table name for editing:", "Edit Record", JOptionPane.PLAIN_MESSAGE);
            if (tableName == null || tableName.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Table name cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        String primaryKeyValue = JOptionPane.showInputDialog(this, "Enter primary key value for the record to edit:", "Edit Record", JOptionPane.PLAIN_MESSAGE);
        if (primaryKeyValue == null || primaryKeyValue.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Primary key value cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            metaData resultMetaData = getMetaData(tableName);

            int result = JOptionPane.showConfirmDialog(this, resultMetaData.panel(), "Edit Record", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                StringBuilder query = new StringBuilder("UPDATE " + tableName + " SET ");

                for (Map.Entry<String, JTextField> entry : resultMetaData.fields().entrySet()) {
                    query.append(entry.getKey()).append("='").append(entry.getValue().getText().replace("'", "''")).append("',");
                }

                query.setLength(query.length() - 1);
                query.append(" WHERE id = ").append(primaryKeyValue).append(";");

                Statement statement = connection.createStatement();
                statement.executeUpdate(query.toString());
                JOptionPane.showMessageDialog(this, "Record updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error editing record: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        loadDatabaseStructure();
    }

    private void deleteRecord(String tableName) {
        if (tableName == null || tableName.trim().isEmpty()) {
            tableName = JOptionPane.showInputDialog(this, "Enter table name for deletion:", "Delete Record", JOptionPane.PLAIN_MESSAGE);
            if (tableName == null || tableName.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Table name cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        String primaryKeyValue = JOptionPane.showInputDialog(this, "Enter primary key value for the record to delete:", "Delete Record", JOptionPane.PLAIN_MESSAGE);
        if (primaryKeyValue == null || primaryKeyValue.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Primary key value cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String query = "DELETE FROM " + tableName + " WHERE id = " + primaryKeyValue;

            Statement statement = connection.createStatement();
            int rowsAffected = statement.executeUpdate(query);
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Record deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "No record found with the given primary key value!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting record: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        loadDatabaseStructure();
    }

    private void searchRecord(String tableName) {
        if (tableName == null || tableName.trim().isEmpty()) {
            tableName = JOptionPane.showInputDialog(this, "Enter table name for searching:", "Search Record", JOptionPane.PLAIN_MESSAGE);
            if (tableName == null || tableName.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Table name cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        String columnName = JOptionPane.showInputDialog(this, "Enter column name to search:", "Search Record", JOptionPane.PLAIN_MESSAGE);
        if (columnName == null || columnName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Column name cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String searchTerm = JOptionPane.showInputDialog(this, "Enter search value:", "Search Record", JOptionPane.PLAIN_MESSAGE);
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Search value cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, tableName, columnName);
            if (!columns.next()) {
                JOptionPane.showMessageDialog(this, "Column not found!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String columnType = columns.getString("TYPE_NAME");
            JOptionPane.showMessageDialog(this, "Column type: " + columnType, "Info", JOptionPane.INFORMATION_MESSAGE);
            String query;

            if (columnType.equals("INTEGER") || columnType.equals("BIGINT") || columnType.equals("SMALLINT") || columnType.equals("serial")) {
                query = "SELECT * FROM " + tableName + " WHERE " + columnName + " = " + Integer.parseInt(searchTerm);
            } else{
                query = "SELECT * FROM " + tableName + " WHERE " + columnName + " LIKE '%" + searchTerm.replace("'", "''") + "%'";
            }

            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            setModelTableView(rs);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error during search: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showDatabaseMetadata() {
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            String info = "Database Product: " + metaData.getDatabaseProductName() + "\n" +
                    "Database Version: " + metaData.getDatabaseProductVersion() + "\n" +
                    "Driver Name: " + metaData.getDriverName() + "\n" +
                    "Driver Version: " + metaData.getDriverVersion();
            JOptionPane.showMessageDialog(this, info, "Database Metadata", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load metadata: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        loadDatabaseStructure();
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this, "Author: Stanislav Humeniuk\nAcademic group: PMI-33\nEmail: stanislav.humeniuk@lnu.edu.ua",
                "About", JOptionPane.INFORMATION_MESSAGE);
        loadDatabaseStructure();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DatabaseManager().setVisible(true));
    }
}
