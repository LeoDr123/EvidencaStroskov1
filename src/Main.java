import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class Main {
    // Parametri za povezavo z bazo
    private static final String PGHOST = "ep-tight-violet-a24qaadz.eu-central-1.aws.neon.tech";
    private static final String PGDATABASE = "baza";
    private static final String PGUSER = "baza_owner";
    private static final String PGPASSWORD = "x3vkYVW9GNze";
    private static final String JDBC_URL = "jdbc:postgresql://" + PGHOST + "/" + PGDATABASE;

    // Metoda za vzpostavitev povezave z bazo
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, PGUSER, PGPASSWORD);
    }

    // Metoda za dodajanje novega uporabnika v podatkovno bazo
    private static boolean registerUser(String firstName, String lastName, String email, String phoneNumber, String password, String selectedCity) {
        // Preverjanje pravilnosti vnesenih podatkov
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phoneNumber.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Prosimo, izpolnite vsa polja.", "Napaka", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Preverjanje, ali je e-poštni naslov veljaven.
        if (!isValidEmail(email)) {
            JOptionPane.showMessageDialog(null, "Vnesen ni veljaven e-poštni naslov.", "Napaka", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try {
            // Ustanovitev povezave s podatkovno bazo
            Connection connection = getConnection();

            // Priprava SQL poizvedbe za vstavljanje novega uporabnika
            String sql = "INSERT INTO uporabniki (ime, priimek, eposta, telefon, geslo, kraj_id) VALUES (?, ?, ?, ?, ?, (SELECT id FROM kraji WHERE ime = ?))";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, firstName);
            statement.setString(2, lastName);
            statement.setString(3, email);
            statement.setString(4, phoneNumber);
            statement.setString(5, password);
            statement.setString(6, selectedCity);

            // Izvedba SQL poizvedbe
            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                JOptionPane.showMessageDialog(null, "Nov uporabnik uspešno dodan.", "Registracija uspešna", JOptionPane.INFORMATION_MESSAGE);
                return true;
            }

            // Zapiranje povezave s podatkovno bazo
            statement.close();
            connection.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Napaka pri registraciji uporabnika: " + e.getMessage(), "Napaka", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    // Metoda za preverjanje veljavnosti e-poštnega naslova
    private static boolean isValidEmail(String email) {
        // Implementacija preprostega preverjanja veljavnosti e-poštnega naslova
        return email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    }

    public static void main(String[] args) {
        // Ustvarimo in prikažemo okno za registracijo
        SwingUtilities.invokeLater(Main::createAndShowGUI);
    }

    // Metoda za ustvarjanje in prikazovanje okna za registracijo
    public static void createAndShowGUI() {
        JFrame frame = new JFrame("Registracija");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400); // Set size to 400x400

        JPanel panel = new JPanel(new GridLayout(7, 2)); // Increase grid rows to 8

        JLabel firstNameLabel = new JLabel("Ime:");
        JTextField firstNameField = new JTextField();
        JLabel lastNameLabel = new JLabel("Priimek:");
        JTextField lastNameField = new JTextField();
        JLabel emailLabel = new JLabel("E-pošta:");
        JTextField emailField = new JTextField();
        JLabel phoneNumberLabel = new JLabel("Telefon:");
        JTextField phoneNumberField = new JTextField();
        JLabel passwordLabel = new JLabel("Geslo:");
        JPasswordField passwordField = new JPasswordField();
        JLabel cityLabel = new JLabel("Kraj:");
        JComboBox<String> cityComboBox = createCityComboBox();

        JButton registerButton = new JButton("Registriraj se");
        JButton loginButton = new JButton("Prijava");

        registerButton.addActionListener(e -> {
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            String email = emailField.getText();
            String phoneNumber = phoneNumberField.getText();
            String password = new String(passwordField.getPassword());
            String selectedCity = (String) cityComboBox.getSelectedItem();
            if (registerUser(firstName, lastName, email, phoneNumber, password, selectedCity)) {
                frame.dispose(); // Close the registration window
                Login.createAndShowGUI(); // Open the login window
            }
        });

        loginButton.addActionListener(e -> {
            frame.dispose(); // Close the registration window
            Login.createAndShowGUI(); // Open the login window
        });

        panel.add(firstNameLabel);
        panel.add(firstNameField);
        panel.add(lastNameLabel);
        panel.add(lastNameField);
        panel.add(emailLabel);
        panel.add(emailField);
        panel.add(phoneNumberLabel);
        panel.add(phoneNumberField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(cityLabel);
        panel.add(cityComboBox);
        panel.add(loginButton);
        panel.add(registerButton);

        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }

    // Metoda za ustvarjanje ComboBoxa s kraji iz podatkovne baze
    private static JComboBox<String> createCityComboBox() {
        JComboBox<String> comboBox = new JComboBox<>();
        try {
            // Ustanovitev povezave s podatkovno bazo
            Connection connection = getConnection();

            // Priprava SQL poizvedbe za pridobitev krajev
            String sql = "SELECT ime FROM kraji";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();

            // Dodajanje imen krajev v ComboBox
            while (resultSet.next()) {
                String cityName = resultSet.getString("ime");
                comboBox.addItem(cityName);
            }

            // Zapiranje povezave s podatkovno bazo
            resultSet.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return comboBox;
    }
}