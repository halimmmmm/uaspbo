package uas;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;

public class Konsumen extends JFrame {
    private JTextField txtIdKonsumen, txtNama, txtAlamat, txtTelepon;
    private JButton btnSimpan, btnEdit, btnHapus, btnRefresh;
    private JTable tableKonsumen;
    private DefaultTableModel model;
    private int selectedRow = -1;

    public Konsumen() {
        setTitle("CRUD Data Konsumen");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Header Label
        JLabel lblTitle = new JLabel("Manajemen Data Konsumen", JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        add(lblTitle, BorderLayout.NORTH);

        // Panel Form untuk input (Bagian Atas)
        JPanel panelForm = new JPanel();
        panelForm.setLayout(new GridLayout(5, 2, 10, 10));
        panelForm.setBorder(BorderFactory.createTitledBorder("Form Data Konsumen"));

        panelForm.add(new JLabel("ID Konsumen:"));
        txtIdKonsumen = new JTextField(20);
        panelForm.add(txtIdKonsumen);

        panelForm.add(new JLabel("Nama Konsumen:"));
        txtNama = new JTextField(20);
        panelForm.add(txtNama);

        panelForm.add(new JLabel("Alamat:"));
        txtAlamat = new JTextField(20);
        panelForm.add(txtAlamat);

        panelForm.add(new JLabel("Telepon:"));
        txtTelepon = new JTextField(20);
        panelForm.add(txtTelepon);

        // Panel Tabel untuk menampilkan data konsumen (Bagian Tengah)
        JPanel panelTable = new JPanel(new BorderLayout());
        panelTable.setBorder(BorderFactory.createTitledBorder("Data Konsumen"));
        tableKonsumen = new JTable();
        model = new DefaultTableModel();
        model.setColumnIdentifiers(new String[]{"ID", "Nama Konsumen", "Alamat", "Telepon"});
        tableKonsumen.setModel(model);
        JScrollPane scrollPane = new JScrollPane(tableKonsumen);
        panelTable.add(scrollPane, BorderLayout.CENTER);

        // Panel Tombol di bawah (Bagian Bawah)
        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnSimpan = new JButton("Simpan");
        btnEdit = new JButton("Edit");
        btnHapus = new JButton("Hapus");
        btnRefresh = new JButton("Refresh");
        panelButtons.add(btnSimpan);
        panelButtons.add(btnEdit);
        panelButtons.add(btnHapus);
        panelButtons.add(btnRefresh);

        // Menambahkan panel ke frame
        add(panelForm, BorderLayout.NORTH);
        add(panelTable, BorderLayout.CENTER);
        add(panelButtons, BorderLayout.SOUTH);

        // Event Handlers
        btnSimpan.addActionListener(e -> simpanKonsumen());
        btnEdit.addActionListener(e -> editKonsumen());
        btnHapus.addActionListener(e -> hapusKonsumen());
        btnRefresh.addActionListener(e -> tampilkanKonsumen());

        // Menampilkan data konsumen otomatis ketika form pertama kali dimuat
        tampilkanKonsumen();

        // Menangani pemilihan baris di tabel
        tableKonsumen.getSelectionModel().addListSelectionListener(e -> {
            selectedRow = tableKonsumen.getSelectedRow();
            if (selectedRow != -1) {
                // Isi data input dengan data yang dipilih dari tabel
                txtIdKonsumen.setText(String.valueOf(model.getValueAt(selectedRow, 0)));
                txtNama.setText((String) model.getValueAt(selectedRow, 1));
                txtAlamat.setText((String) model.getValueAt(selectedRow, 2));
                txtTelepon.setText((String) model.getValueAt(selectedRow, 3));
            }
        });
    }

    private void simpanKonsumen() {
        String idKonsumen = txtIdKonsumen.getText();
        String nama = txtNama.getText();
        String alamat = txtAlamat.getText();
        String telepon = txtTelepon.getText();

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/uas_crud", "root", "");
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO konsumen (id_konsumen, nama_konsumen, alamat, telepon) VALUES (?, ?, ?, ?)")) {
            stmt.setInt(1, Integer.parseInt(idKonsumen));
            stmt.setString(2, nama);
            stmt.setString(3, alamat);
            stmt.setString(4, telepon);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Data berhasil disimpan!");
            tampilkanKonsumen();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void editKonsumen() {
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih konsumen yang ingin diedit.");
            return;
        }

        String idKonsumen = txtIdKonsumen.getText();
        String nama = txtNama.getText();
        String alamat = txtAlamat.getText();
        String telepon = txtTelepon.getText();

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/uas_crud", "root", "");
             PreparedStatement stmt = conn.prepareStatement("UPDATE konsumen SET nama_konsumen = ?, alamat = ?, telepon = ? WHERE id_konsumen = ?")) {
            stmt.setString(1, nama);
            stmt.setString(2, alamat);
            stmt.setString(3, telepon);
            stmt.setInt(4, Integer.parseInt(idKonsumen));
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Data berhasil diupdate!");
            tampilkanKonsumen();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void hapusKonsumen() {
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih konsumen yang ingin dihapus.");
            return;
        }

        int idKonsumen = Integer.parseInt(txtIdKonsumen.getText());

        int confirm = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin menghapus konsumen dengan ID: " + idKonsumen + "?",
                "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/uas_crud", "root", "");
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM konsumen WHERE id_konsumen = ?")) {
                stmt.setInt(1, idKonsumen);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Data berhasil dihapus!");
                tampilkanKonsumen();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }

    private void tampilkanKonsumen() {
        model.setRowCount(0);
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/uas_crud", "root", "");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM konsumen")) {
            while (rs.next()) {
                model.addRow(new Object[]{rs.getInt("id_konsumen"), rs.getString("nama_konsumen"), rs.getString("alamat"), rs.getString("telepon")});
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Konsumen().setVisible(true));
    }
}