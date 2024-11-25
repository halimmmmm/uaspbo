package uas;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;

public class Barang extends JFrame {
    private JTextField txtIdBarang, txtNama, txtHarga, txtStok;
    private JButton btnSimpan, btnEdit, btnHapus, btnRefresh;
    private JTable tableBarang;
    private DefaultTableModel model;
    private int selectedRow = -1;

    public Barang() {
        setTitle("CRUD Data Barang");
        setSize(800, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Header Label
        JLabel lblTitle = new JLabel("Manajemen Data Barang", JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        add(lblTitle, BorderLayout.NORTH);

        // Panel Form untuk input
        JPanel panelForm = new JPanel();
        panelForm.setLayout(new GridLayout(4, 2, 10, 10));
        panelForm.setBorder(BorderFactory.createTitledBorder("Form Data Barang"));

        panelForm.add(new JLabel("ID Barang:"));
        txtIdBarang = new JTextField(20);
        panelForm.add(txtIdBarang);

        panelForm.add(new JLabel("Nama Barang:"));
        txtNama = new JTextField(20);
        panelForm.add(txtNama);

        panelForm.add(new JLabel("Harga:"));
        txtHarga = new JTextField(20);
        panelForm.add(txtHarga);

        panelForm.add(new JLabel("Stok:"));
        txtStok = new JTextField(20);
        panelForm.add(txtStok);

        // Panel Tabel untuk menampilkan data barang
        JPanel panelTable = new JPanel(new BorderLayout());
        panelTable.setBorder(BorderFactory.createTitledBorder("Data Barang"));
        tableBarang = new JTable();
        model = new DefaultTableModel();
        model.setColumnIdentifiers(new String[]{"ID", "Nama Barang", "Harga", "Stok"});
        tableBarang.setModel(model);
        JScrollPane scrollPane = new JScrollPane(tableBarang);
        panelTable.add(scrollPane, BorderLayout.CENTER);

        // Panel Tombol
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
        btnSimpan.addActionListener(e -> simpanBarang());
        btnEdit.addActionListener(e -> editBarang());
        btnHapus.addActionListener(e -> hapusBarang());
        btnRefresh.addActionListener(e -> tampilkanBarang());

        // Menampilkan data barang otomatis ketika form pertama kali dimuat
        tampilkanBarang();

        // Menangani pemilihan baris di tabel
        tableBarang.getSelectionModel().addListSelectionListener(e -> {
            selectedRow = tableBarang.getSelectedRow();
            if (selectedRow != -1) {
                txtIdBarang.setText(String.valueOf(model.getValueAt(selectedRow, 0)));
                txtNama.setText((String) model.getValueAt(selectedRow, 1));
                txtHarga.setText(String.valueOf(model.getValueAt(selectedRow, 2)));
                txtStok.setText(String.valueOf(model.getValueAt(selectedRow, 3)));
            }
        });
    }

    private void simpanBarang() {
        String idBarangText = txtIdBarang.getText();
        String nama = txtNama.getText();
        int harga = Integer.parseInt(txtHarga.getText());
        int stok = Integer.parseInt(txtStok.getText());

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/uas_crud", "root", "");
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO barang (id_barang, nama_barang, harga, stok) VALUES (?, ?, ?, ?)")) {
            stmt.setInt(1, Integer.parseInt(idBarangText));
            stmt.setString(2, nama);
            stmt.setInt(3, harga);
            stmt.setInt(4, stok);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Data berhasil disimpan!");
            tampilkanBarang();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void editBarang() {
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih barang yang ingin diedit.");
            return;
        }

        int idBarang = Integer.parseInt(txtIdBarang.getText());
        String nama = txtNama.getText();
        int harga = Integer.parseInt(txtHarga.getText());
        int stok = Integer.parseInt(txtStok.getText());

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/uas_crud", "root", "");
             PreparedStatement stmt = conn.prepareStatement("UPDATE barang SET nama_barang = ?, harga = ?, stok = ? WHERE id_barang = ?")) {
            stmt.setString(1, nama);
            stmt.setInt(2, harga);
            stmt.setInt(3, stok);
            stmt.setInt(4, idBarang);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Data berhasil diupdate!");
            tampilkanBarang();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void hapusBarang() {
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih barang yang ingin dihapus.");
            return;
        }

        int idBarang = Integer.parseInt(txtIdBarang.getText());

        int confirm = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin menghapus barang dengan ID: " + idBarang + "?",
                "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/uas_crud", "root", "");
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM barang WHERE id_barang = ?")) {
                stmt.setInt(1, idBarang);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Data berhasil dihapus!");
                tampilkanBarang();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }

    private void tampilkanBarang() {
        model.setRowCount(0);
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/uas_crud", "root", "");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM barang")) {
            while (rs.next()) {
                model.addRow(new Object[]{rs.getInt("id_barang"), rs.getString("nama_barang"), rs.getInt("harga"), rs.getInt("stok")});
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Barang().setVisible(true));
    }
}