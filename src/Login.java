import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class Login {
    // Database connection parameters
    private static final String PGHOST = "ep-tight-violet-a24qaadz.eu-central-1.aws.neon.tech";
    private static final String PGDATABASE = "baza";
    private static final String PGUSER = "baza_owner";
    private static final String PGPASSWORD = "x3vkYVW9GNze";
    private static final String JDBC_URL = "jdbc:postgresql://" + PGHOST + "/" + PGDATABASE;

    // Metoda za vzpostavitev povezave z bazo
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, PGUSER, PGPASSWORD);
    }

    // Metoda za preverjanje pravilnosti prijave uporabnika
    private static int loginUserAndGetId(String email, String password) {
        try {
            // Ustanovitev povezave s podatkovno bazo.
            Connection connection = getConnection();

            // Priprava SQL poizvedbe za preverjanje prijave uporabnika
            String sql = "SELECT id FROM uporabniki WHERE eposta = ? AND geslo = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, email);
            statement.setString(2, password);

            // Izvedba SQL poizvedbe
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int userId = resultSet.getInt("id");
                resultSet.close();
                statement.close();
                connection.close();
                return userId; // Uspešna prijava
            } else {
                resultSet.close();
                statement.close();
                connection.close();
                return -1; // Neuspešna prijava
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Napaka pri prijavi: " + e.getMessage(), "Napaka", JOptionPane.ERROR_MESSAGE);
            return -1; // Neuspešna prijava
        }
    }

    public static void createAndShowGUI() {
        JFrame frame = new JFrame("Prijava");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300); // Set size to 400x300

        JPanel panel = new JPanel(new GridLayout(3, 2));

        JLabel emailLabel = new JLabel("E-pošta:");
        JTextField emailField = new JTextField();
        JLabel passwordLabel = new JLabel("Geslo:");
        JPasswordField passwordField = new JPasswordField();

        JButton loginButton = new JButton("Prijava");
        JButton signupButton = new JButton("Registracija");

        loginButton.addActionListener(e -> {
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());
            int userId = loginUserAndGetId(email, password);
            if (userId != -1) {
                frame.dispose();

                domacastran.createAndShowGUI(userId); // Pass user's ID to the home screen
            }
        });

        signupButton.addActionListener(e -> {
            frame.dispose(); // Close the login window
            Main.createAndShowGUI(); // Open the signup window
        });

        panel.add(emailLabel);
        panel.add(emailField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(signupButton);
        panel.add(loginButton);

        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Login::createAndShowGUI);
    }
}
