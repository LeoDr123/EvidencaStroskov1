import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class DodajPodjetje {
    // Database connection parameters
    private static final String PGHOST = "ep-tight-violet-a24qaadz.eu-central-1.aws.neon.tech";
    private static final String PGDATABASE = "baza";
    private static final String PGUSER = "baza_owner";
    private static final String PGPASSWORD = "x3vkYVW9GNze";
    private static final String JDBC_URL = "jdbc:postgresql://" + PGHOST + "/" + PGDATABASE;

    private JComboBox<String> companyComboBox; // Declare JComboBox as a class variable

    public DodajPodjetje(JFrame parentFrame, JComboBox<String> companyComboBox) {
        this.companyComboBox = companyComboBox; // Assign the JComboBox passed from DodajPodatke
        JFrame frame = new JFrame("Dodaj Podjetje");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(300, 150); // Set size to 300x150
        frame.setLocationRelativeTo(parentFrame);

        JPanel panel = new JPanel(new GridLayout(3, 2));

        JLabel nameLabel = new JLabel("Ime podjetja:");
        JTextField nameField = new JTextField();
        JLabel cityLabel = new JLabel("Kraj:");
        JComboBox<String> cityComboBox = new JComboBox<>(fetchCities());
        JButton addButton = new JButton("Dodaj");

        panel.add(nameLabel);
        panel.add(nameField);
        panel.add(cityLabel);
        panel.add(cityComboBox);
        panel.add(new JLabel()); // Empty label for spacing
        panel.add(addButton);

        addButton.addActionListener(e -> {
            try {
                Connection conn = DriverManager.getConnection(JDBC_URL, PGUSER, PGPASSWORD);
                String name = nameField.getText();
                String selectedCity = (String) cityComboBox.getSelectedItem();

                // Fetch the ID of the selected city
                int cityId = getCityId(selectedCity);

                // Insert the new company into the database
                String insertQuery = "INSERT INTO podjetja (ime, kraj_id) VALUES (?, ?)";
                PreparedStatement insertStatement = conn.prepareStatement(insertQuery);
                insertStatement.setString(1, name);
                insertStatement.setInt(2, cityId);
                insertStatement.executeUpdate();
                insertStatement.close();

                // Close the frame after successful insertion
                frame.dispose();

                // Refresh the companyComboBox in DodajPodatke.java
                refreshCompanyComboBox();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Napaka pri dodajanju podjetja: " + ex.getMessage(), "Napaka", JOptionPane.ERROR_MESSAGE);
            }
        });

        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }

    private String[] fetchCities() {
        try {
            Connection conn = DriverManager.getConnection(JDBC_URL, PGUSER, PGPASSWORD);
            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = stmt.executeQuery("SELECT ime FROM kraji");

            // Move to the last row to get the row count
            rs.last();
            int numRows = rs.getRow();
            rs.beforeFirst();

            // Create an array to store city names
            String[] cities = new String[numRows];

            // Iterate through the ResultSet and add city names to the array
            int index = 0;
            while (rs.next()) {
                cities[index++] = rs.getString("ime");
            }

            // Close ResultSet, Statement, and Connection
            rs.close();
            stmt.close();
            conn.close();

            return cities;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Napaka pri pridobivanju krajev: " + e.getMessage(), "Napaka", JOptionPane.ERROR_MESSAGE);
            return new String[0];
        }
    }

    private int getCityId(String cityName) {
        try {
            Connection conn = DriverManager.getConnection(JDBC_URL, PGUSER, PGPASSWORD);
            PreparedStatement stmt = conn.prepareStatement("SELECT id FROM kraji WHERE ime = ?");
            stmt.setString(1, cityName);
            ResultSet rs = stmt.executeQuery();
            int cityId = -1;
            if (rs.next()) {
                cityId = rs.getInt("id");
            }
            rs.close();
            stmt.close();
            conn.close();
            return cityId;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Napaka pri pridobivanju ID-ja kraja: " + e.getMessage(), "Napaka", JOptionPane.ERROR_MESSAGE);
            return -1;
        }
    }

    private void refreshCompanyComboBox() {
        // Refresh the companyComboBox by fetching the updated list of company names from the database
        String[] companyNames = fetchCompanyNames();
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(companyNames);
        companyComboBox.setModel(model);
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

            // Create an array to store company names
            String[] companyNames = new String[numRows];

            // Iterate through the ResultSet and add company names to the array
            int index = 0;
            while (rs.next()) {
                companyNames[index++] = rs.getString("ime");
            }

            // Close ResultSet, Statement, and Connection
            rs.close();
            stmt.close();
            conn.close();

            return companyNames;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Napaka pri pridobivanju podjetij: " + e.getMessage(), "Napaka", JOptionPane.ERROR_MESSAGE);
            return new String[0];
        }
    }
}
