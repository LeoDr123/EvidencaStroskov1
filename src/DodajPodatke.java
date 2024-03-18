// DodajPodatke.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import org.jdesktop.swingx.JXDatePicker;

public class DodajPodatke {
    // Database connection parameters
    private static final String PGHOST = "ep-tight-violet-a24qaadz.eu-central-1.aws.neon.tech";
    private static final String PGDATABASE = "baza";
    private static final String PGUSER = "baza_owner";
    private static final String PGPASSWORD = "x3vkYVW9GNze";
    private static final String JDBC_URL = "jdbc:postgresql://" + PGHOST + "/" + PGDATABASE;

    public DodajPodatke(JFrame parentFrame, int userId) {
        JFrame frame = new JFrame("Dodaj Podatke");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 300); // Set size to 400x300
        frame.setLocationRelativeTo(parentFrame);

        JPanel panel = new JPanel(new GridLayout(7, 2));

        JLabel typeLabel = new JLabel("Vrsta:");
        JComboBox<String> typeComboBox = new JComboBox<>(new String[]{"Prihodek", "Poraba"});
        JLabel dateLabel = new JLabel("Datum:");
        JXDatePicker datePicker = new JXDatePicker();
        datePicker.setFormats("dd.MM.yyyy");
        JLabel descriptionLabel = new JLabel("Opis:");
        JTextField descriptionField = new JTextField();
        JLabel amountLabel = new JLabel("Znesek:");
        JTextField amountField = new JTextField();
        JLabel companyLabel = new JLabel("Podjetje:");
        JComboBox<String> companyComboBox = new JComboBox<>(fetchCompanyNames());
        JButton addButton = new JButton("Dodaj");
        JButton addCompanyButton = new JButton("Dodaj Podjetje"); // Button to open the DodajPodjetje.java window

        panel.add(typeLabel);
        panel.add(typeComboBox);
        panel.add(dateLabel);
        panel.add(datePicker);
        panel.add(descriptionLabel);
        panel.add(descriptionField);
        panel.add(amountLabel);
        panel.add(amountField);
        panel.add(companyLabel);
        panel.add(companyComboBox);
        panel.add(addCompanyButton); // Add button to the panel
        panel.add(addButton);

        // Action listener for the addCompanyButton
        addCompanyButton.addActionListener(e -> {
            // Open the DodajPodjetje.java window and pass the companyComboBox reference
            new DodajPodjetje(frame, companyComboBox);
        });

        addButton.addActionListener(e -> {
            try {
                Connection conn = DriverManager.getConnection(JDBC_URL, PGUSER, PGPASSWORD);
                double amount = Double.parseDouble(amountField.getText());
                double currentBalance = fetchAccountBalance(userId);
                double newBalance;

                if (typeComboBox.getSelectedItem().equals("Prihodek")) {
                    newBalance = currentBalance + amount;
                } else {
                    newBalance = currentBalance - amount;
                }

                String insertQuery;
                if (typeComboBox.getSelectedItem().equals("Prihodek")) {
                    insertQuery = "{CALL insert_racuni_prihodek(?, ?, ?, ?, ?)}";
                } else {
                    insertQuery = "{CALL insert_racuni_porabnina(?, ?, ?, ?, ?)}";
                    amount *= -1;
                }
                CallableStatement insertStatement = conn.prepareCall(insertQuery);
                insertStatement.setDate(1, new java.sql.Date(datePicker.getDate().getTime()));
                insertStatement.setString(2, descriptionField.getText());
                insertStatement.setDouble(3, amount);
                insertStatement.setInt(4, userId);
                insertStatement.setInt(5, getSelectedCompanyId((String) companyComboBox.getSelectedItem()));
                insertStatement.execute();
                insertStatement.close();

                updateAccountBalance(conn, userId, newBalance);

                frame.dispose();
                domacastran.refreshTable(userId);
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Napaka pri dodajanju vnosa: " + ex.getMessage(), "Napaka", JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Znesek mora biti številka.", "Napaka", JOptionPane.ERROR_MESSAGE);
            }
        });


        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }

    private String[] fetchCompanyNames() {
        try {
            Connection conn = DriverManager.getConnection(JDBC_URL, PGUSER, PGPASSWORD);
            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = stmt.executeQuery("SELECT ime FROM podjetja");

            // Move to the last row to get the row count
            rs.last();
            int numRows = rs.getRow();
            rs.beforeFirst();

            // Create an ArrayList to store company names
            ArrayList<String> companyList = new ArrayList<>();

            // Iterate through the ResultSet and add company names to the list
            while (rs.next()) {
                companyList.add(rs.getString("ime"));
            }

            // Close ResultSet, Statement, and Connection
            rs.close();
            stmt.close();
            conn.close();

            // Convert ArrayList to array
            String[] companyNames = companyList.toArray(new String[0]);

            return companyNames;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Napaka pri pridobivanju podjetij: " + e.getMessage(), "Napaka", JOptionPane.ERROR_MESSAGE);
            return new String[0];
        }
    }

    private double fetchAccountBalance(int userId) {
        double accountBalance = 0.0;
        try {
            Connection connection = DriverManager.getConnection(JDBC_URL, PGUSER, PGPASSWORD);
            String query = "SELECT stanje FROM racuni WHERE uporabnik_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                accountBalance = resultSet.getDouble("stanje");
            }
            resultSet.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Napaka pri pridobivanju stanja na računu: " + e.getMessage(), "Napaka", JOptionPane.ERROR_MESSAGE);
        }
        return accountBalance;
    }

    private void updateAccountBalance(Connection conn, int userId, double newBalance) {
        try {
            String query = "UPDATE racuni SET stanje = ? WHERE uporabnik_id = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setDouble(1, newBalance);
            statement.setInt(2, userId);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Napaka pri posodabljanju stanja na računu: " + e.getMessage(), "Napaka", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int getSelectedCompanyId(String companyName) {
        try {
            Connection conn = DriverManager.getConnection(JDBC_URL, PGUSER, PGPASSWORD);
            PreparedStatement stmt = conn.prepareStatement("SELECT id FROM podjetja WHERE ime = ?");
            stmt.setString(1, companyName);
            ResultSet rs = stmt.executeQuery();
            int companyId = -1;
            if (rs.next()) {
                companyId = rs.getInt("id");
            }
            rs.close();
            stmt.close();
            conn.close();
            return companyId;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Napaka pri pridobivanju ID-ja podjetja: " + e.getMessage(), "Napaka", JOptionPane.ERROR_MESSAGE);
            return -1;
        }
    }
}
