import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;


public class domacastran {
    private static final String PGHOST = "ep-tight-violet-a24qaadz.eu-central-1.aws.neon.tech";
    private static final String PGDATABASE = "baza";
    private static final String PGUSER = "baza_owner";
    private static final String PGPASSWORD = "x3vkYVW9GNze";
    private static final String JDBC_URL = "jdbc:postgresql://" + PGHOST + "/" + PGDATABASE;
    private static JTable table;
    private static JLabel balanceLabel;

    public static void createAndShowGUI(int userId) {
        JFrame frame = new JFrame("Domača Stran");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400);

        JPanel panel = new JPanel(new BorderLayout());

        double accountBalance = fetchAccountBalance(userId);
        balanceLabel = new JLabel("Stanje na računu: " + accountBalance + " €");
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(balanceLabel, BorderLayout.NORTH);

        JLabel label = new JLabel("Dobrodošli na Domači Strani!");
        label.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(label, BorderLayout.CENTER);

        // Create table model
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        model.addColumn("Datum");
        model.addColumn("Opis");
        model.addColumn("Znesek");
        model.addColumn("Vrsta transakcije");
        model.addColumn("Podjetje");
        model.addColumn("Uredi");
        model.addColumn("Izbriši");

        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(300);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);

        // Set custom cell renderer for the "Uredi" and "Izbriši" column
        table.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer());
        table.addMouseListener(new ButtonClickListener(table, userId, JDBC_URL, PGUSER, PGPASSWORD));

        JButton addButton = new JButton("Dodaj podatke");
        addButton.addActionListener(e -> {
            new DodajPodatke(frame, userId);
        });

        panel.add(addButton, BorderLayout.SOUTH);

        frame.getContentPane().add(panel);
        frame.setVisible(true);

        refreshTable(userId);
    }

    public static void refreshTable(int userId) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        try {
            Connection connection = DriverManager.getConnection(JDBC_URL, PGUSER, PGPASSWORD);
            String query = "SELECT r.datum, r.opis, r.znesek, COALESCE(p.ime, 'N/A') AS podjetje, CASE WHEN r.prihodek_id IS NOT NULL THEN 'Poraba' ELSE 'Prihodek' END AS vrsta_transakcije FROM racuni r LEFT JOIN podjetja p ON r.podjetje_id = p.id WHERE r.uporabnik_id = ?";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String date = resultSet.getString("datum");
                String description = resultSet.getString("opis");
                double amount = resultSet.getDouble("znesek");
                String transactionType = resultSet.getString("vrsta_transakcije");
                String companyName = resultSet.getString("podjetje");

                model.addRow(new Object[]{date, description, amount, transactionType, companyName, "Uredi", "Izbriši"});
            }

            resultSet.close();
            statement.close();
            connection.close();

            double accountBalance = fetchAccountBalance(userId);
            balanceLabel.setText("Stanje na računu: " + accountBalance + " €");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Napaka pri pridobivanju podatkov iz baze: " + e.getMessage(), "Napaka", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static double fetchAccountBalance(int userId) {
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createAndShowGUI(1));
    }
}

class ButtonRenderer extends JButton implements TableCellRenderer {
    public ButtonRenderer() {
        setOpaque(true);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (isSelected) {
            setForeground(table.getSelectionForeground());
            setBackground(table.getSelectionBackground());
        } else {
            setForeground(table.getForeground());
            setBackground(UIManager.getColor("Button.background"));
        }
        setText((value == null) ? "" : value.toString());
        return this;
    }
}

class ButtonClickListener extends MouseAdapter {
    private final JTable table;
    private final int userId;
    private final String jdbcUrl;
    private final String pgUser;
    private final String pgPassword;

    public ButtonClickListener(JTable table, int userId, String jdbcUrl, String pgUser, String pgPassword) {
        this.table = table;
        this.userId = userId;
        this.jdbcUrl = jdbcUrl;
        this.pgUser = pgUser;
        this.pgPassword = pgPassword;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int columnEdit = table.getColumnModel().getColumnIndex("Uredi");
        int columnDelete = table.getColumnModel().getColumnIndex("Izbriši");
        int row = table.rowAtPoint(e.getPoint());

        if (row >= 0 && e.getClickCount() == 2) {
            if (table.columnAtPoint(e.getPoint()) == columnEdit) {
                editTransaction(row);
            }
        }

        if (row >= 0 && e.getClickCount() == 1) {
            if (table.columnAtPoint(e.getPoint()) == columnDelete) {
                deleteTransaction(row);
            }
        }
    }

    private void editTransaction(int row) {
        String date = table.getValueAt(row, 0).toString();
        String description = table.getValueAt(row, 1).toString();
        double amount = Double.parseDouble(table.getValueAt(row, 2).toString());
        new UrediPodatke(date, description, amount, userId);
    }

    private void deleteTransaction(int row) {
        String date = table.getValueAt(row, 0).toString();
        String description = table.getValueAt(row, 1).toString();
        int choice = JOptionPane.showConfirmDialog(null, "Ali ste prepričani, da želite izbrisati to transakcijo?", "Potrditev brisanja", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            try {
                Connection connection = DriverManager.getConnection(jdbcUrl, pgUser, pgPassword);

                // Klic shranjenega postopka
                String callProcedure = "{CALL IzbrisiRacune(?, ?, ?)}";
                CallableStatement statement = connection.prepareCall(callProcedure);
                statement.setInt(1, userId);

                // Pretvorba niza v java.sql.Date
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                java.sql.Date parsedDate = new java.sql.Date(format.parse(date).getTime());

                // Nastavitev parametrov klica shranjenega postopka
                statement.setDate(2, parsedDate);
                statement.setString(3, description);
                statement.execute();

                JOptionPane.showMessageDialog(null, "Transakcija uspešno izbrisana.");
                domacastran.refreshTable(userId);

                statement.close();
                connection.close();
            } catch (SQLException | ParseException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Napaka pri brisanju transakcije: " + ex.getMessage(), "Napaka", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


}
