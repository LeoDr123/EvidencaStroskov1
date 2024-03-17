import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class domacastran {
    // Database connection parameters
    private static final String PGHOST = "ep-tight-violet-a24qaadz.eu-central-1.aws.neon.tech";
    private static final String PGDATABASE = "baza";
    private static final String PGUSER = "baza_owner";
    private static final String PGPASSWORD = "x3vkYVW9GNze";
    private static final String JDBC_URL = "jdbc:postgresql://" + PGHOST + "/" + PGDATABASE;

    public static void createAndShowGUI(int userId) {
        JFrame frame = new JFrame("Domača Stran");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400); // Set size to 800x400

        JPanel panel = new JPanel(new BorderLayout());

        // Header label
        JLabel label = new JLabel("Dobrodošli na Domači Strani!");
        label.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(label, BorderLayout.NORTH);

        // Table to display user's expenses
        JTable table = new JTable();
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Fetch and display user's expenses
        try {
            Connection connection = DriverManager.getConnection(JDBC_URL, PGUSER, PGPASSWORD);
            String query = "SELECT * FROM racuni WHERE uporabnik_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();

            // Create table model
            DefaultTableModel model = new DefaultTableModel() {
                // Override isCellEditable method to make all cells non-editable except the button column
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false; // Disable editing for all cells
                }
            };
            table.setModel(model);

            // Add columns
            model.addColumn("Datum");
            model.addColumn("Opis");
            model.addColumn("Znesek");
            model.addColumn("Uredi"); // Button column

            // Set column widths
            table.getColumnModel().getColumn(0).setPreferredWidth(100); // Datum
            table.getColumnModel().getColumn(1).setPreferredWidth(300); // Opis
            table.getColumnModel().getColumn(2).setPreferredWidth(100); // Znesek
            table.getColumnModel().getColumn(3).setPreferredWidth(80); // Uredi (Button column)

            // Add rows
            while (resultSet.next()) {
                String date = resultSet.getString("datum");
                String description = resultSet.getString("opis");
                double amount = resultSet.getDouble("znesek");

                // Add the button text to the button column
                model.addRow(new Object[]{date, description, amount, "Uredi"});
            }

            // Set custom renderer for the button column
            table.getColumnModel().getColumn(3).setCellRenderer(new ButtonRenderer());

            // Add action listener to handle button clicks
            table.addMouseListener(new ButtonClickListener(table));

            resultSet.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Napaka pri pridobivanju podatkov iz baze: " + e.getMessage(), "Napaka", JOptionPane.ERROR_MESSAGE);
        }

        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createAndShowGUI(1));
    }
}

class ButtonRenderer extends JButton implements TableCellRenderer {
    public ButtonRenderer() {
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setText((value == null) ? "" : value.toString());
        return this;
    }
}

class ButtonClickListener extends MouseAdapter {
    private final JTable table;

    public ButtonClickListener(JTable table) {
        this.table = table;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int column = table.getColumnModel().getColumnIndexAtX(e.getX());
        int row = e.getY() / table.getRowHeight();

        if (row < table.getRowCount() && row >= 0 && column < table.getColumnCount() && column >= 0) {
            Object value = table.getValueAt(row, column);
            if (value instanceof String && "Uredi".equals(value)) {
                // Handle button click (open editing window)
                String date = (String) table.getValueAt(row, 0);
                String description = (String) table.getValueAt(row, 1);
                double amount = (Double) table.getValueAt(row, 2);

                JFrame editFrame = new JFrame("Uredi");
                editFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                // Open the editing window UrediPodatke.java
                new UrediPodatke(date, description, amount);
            }
        }
    }
}
