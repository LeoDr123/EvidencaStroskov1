import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class UrediPodatke {
    private String date;
    private String description;
    private double amount;

    public UrediPodatke(String date, String description, double amount) {
        this.date = date;
        this.description = description;
        this.amount = amount;

        JFrame frame = new JFrame("Uredi Podatke");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 300);

        JPanel panel = new JPanel(new GridLayout(4, 2));

        JLabel dateLabel = new JLabel("Datum:");
        JTextField dateField = new JTextField(date);
        JLabel descriptionLabel = new JLabel("Opis:");
        JTextField descriptionField = new JTextField(description);
        JLabel amountLabel = new JLabel("Znesek:");
        JTextField amountField = new JTextField(String.valueOf(amount));
        JButton saveButton = new JButton("Shrani");

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Update the data in the database
                try {
                    Connection connection = DriverManager.getConnection(JDBC_URL, PGUSER, PGPASSWORD);
                    String query = "UPDATE racuni SET datum = ?, opis = ?, znesek = ? WHERE datum = ? AND opis = ? AND znesek = ?";
                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.setString(1, dateField.getText());
                    statement.setString(2, descriptionField.getText());
                    statement.setDouble(3, Double.parseDouble(amountField.getText()));
                    statement.setString(4, date);
                    statement.setString(5, description);
                    statement.setDouble(6, amount);

                    int rowsAffected = statement.executeUpdate();
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(null, "Podatki uspešno posodobljeni.", "Posodobitev uspešna", JOptionPane.INFORMATION_MESSAGE);
                        frame.dispose(); // Close the edit window
                    } else {
                        JOptionPane.showMessageDialog(null, "Napaka pri posodabljanju podatkov.", "Napaka", JOptionPane.ERROR_MESSAGE);
                    }

                    statement.close();
                    connection.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Napaka pri posodabljanju podatkov: " + ex.getMessage(), "Napaka", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        panel.add(dateLabel);
        panel.add(dateField);
        panel.add(descriptionLabel);
        panel.add(descriptionField);
        panel.add(amountLabel);
        panel.add(amountField);
        panel.add(new JLabel());
        panel.add(saveButton);

        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }


    private static final String PGHOST = "ep-tight-violet-a24qaadz.eu-central-1.aws.neon.tech";
    private static final String PGDATABASE = "baza";
    private static final String PGUSER = "baza_owner";
    private static final String PGPASSWORD = "x3vkYVW9GNze";
    private static final String JDBC_URL = "jdbc:postgresql://" + PGHOST + "/" + PGDATABASE;
}
