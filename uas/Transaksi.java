package uas;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;

public class Transaksi extends JFrame {
    private JComboBox<String> cbBarang, cbKonsumen;
    private JTextField txtJumlah, txtTanggal, txtTotalHarga;
    private JButton btnSimpan, btnEdit, btnHapus, btnTampilkan;
    private JTable tableTransaksi;
    private DefaultTableModel model;

    public Transaksi() {
        setTitle("Manajemen Data Transaksi");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel Input
        JPanel panelInput = new JPanel(new GridLayout(5, 2, 10, 10));
        panelInput.setBorder(BorderFactory.createTitledBorder("Form Transaksi"));

        cbBarang = new JComboBox<>();
        cbKonsumen = new JComboBox<>();
        txtJumlah = new JTextField();
        txtTotalHarga = new JTextField();
        txtTanggal = new JTextField();

        txtTotalHarga.setEditable(false);

        panelInput.add(new JLabel("Barang:"));
        panelInput.add(cbBarang);
        panelInput.add(new JLabel("Konsumen:"));
        panelInput.add(cbKonsumen);
        panelInput.add(new JLabel("Jumlah:"));
        panelInput.add(txtJumlah);
        panelInput.add(new JLabel("Total Harga:"));
        panelInput.add(txtTotalHarga);
        panelInput.add(new JLabel("Tanggal (YYYY-MM-DD):"));
        panelInput.add(txtTanggal);

        // Panel Tabel
        JPanel panelTabel = new JPanel(new BorderLayout());
        panelTabel.setBorder(BorderFactory.createTitledBorder("Data Transaksi"));

        tableTransaksi = new JTable();
        model = new DefaultTableModel(new String[]{"ID", "Barang", "Konsumen", "Jumlah", "Total Harga", "Tanggal"}, 0);
        tableTransaksi.setModel(model);

        JScrollPane scrollPane = new JScrollPane(tableTransaksi);
        panelTabel.add(scrollPane, BorderLayout.CENTER);

        // Panel Tombol
        JPanel panelTombol = new JPanel(new FlowLayout());
        btnSimpan = new JButton("Simpan");
        btnEdit = new JButton("Edit");
        btnHapus = new JButton("Hapus");
        btnTampilkan = new JButton("Tampilkan");

        panelTombol.add(btnSimpan);
        panelTombol.add(btnEdit);
        panelTombol.add(btnHapus);
        panelTombol.add(btnTampilkan);

        add(panelInput, BorderLayout.NORTH);
        add(panelTabel, BorderLayout.CENTER);
        add(panelTombol, BorderLayout.SOUTH);

        // Event Listener
        btnSimpan.addActionListener(e -> simpanTransaksi());
        btnEdit.addActionListener(e -> editTransaksi());
        btnHapus.addActionListener(e -> hapusTransaksi());
        btnTampilkan.addActionListener(e -> tampilkanTransaksi());

        tableTransaksi.getSelectionModel().addListSelectionListener(e -> isiFormDariTabel());

        loadDataComboBox();
        tampilkanTransaksi();
    }

    private void loadDataComboBox() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/uas_crud", "root", "")) {
            Statement stmtBarang = conn.createStatement();
            ResultSet rsBarang = stmtBarang.executeQuery("SELECT nama_barang FROM barang");
            while (rsBarang.next()) {
                cbBarang.addItem(rsBarang.getString("nama_barang"));
            }

            Statement stmtKonsumen = conn.createStatement();
            ResultSet rsKonsumen = stmtKonsumen.executeQuery("SELECT nama_konsumen FROM konsumen");
            while (rsKonsumen.next()) {
                cbKonsumen.addItem(rsKonsumen.getString("nama_konsumen"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + ex.getMessage());
        }
    }

    private void simpanTransaksi() {
        String barang = (String) cbBarang.getSelectedItem();
        String konsumen = (String) cbKonsumen.getSelectedItem();
        int jumlah = Integer.parseInt(txtJumlah.getText());
        String tanggal = txtTanggal.getText();

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/uas_crud", "root", "")) {
            PreparedStatement stmtHarga = conn.prepareStatement("SELECT harga FROM barang WHERE nama_barang = ?");
            stmtHarga.setString(1, barang);
            ResultSet rs = stmtHarga.executeQuery();
            rs.next();
            int harga = rs.getInt("harga");
            int totalHarga = harga * jumlah;

            txtTotalHarga.setText(String.valueOf(totalHarga));

            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO transaksi (id_barang, id_konsumen, jumlah, total_harga, tanggal) " +
                "SELECT b.id_barang, k.id_konsumen, ?, ?, ? FROM barang b, konsumen k " +
                "WHERE b.nama_barang = ? AND k.nama_konsumen = ?"
            );
            stmt.setInt(1, jumlah);
            stmt.setInt(2, totalHarga);
            stmt.setString(3, tanggal);
            stmt.setString(4, barang);
            stmt.setString(5, konsumen);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Data berhasil disimpan!");
            tampilkanTransaksi();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void editTransaksi() {
        int selectedRow = tableTransaksi.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih transaksi yang ingin diubah!");
            return;
        }

        int idTransaksi = (int) model.getValueAt(selectedRow, 0); // Ambil ID transaksi dari tabel
        String barang = (String) cbBarang.getSelectedItem();
        String konsumen = (String) cbKonsumen.getSelectedItem();
        int jumlah = Integer.parseInt(txtJumlah.getText());
        String tanggal = txtTanggal.getText();

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/uas_crud", "root", "")) {
            // Mendapatkan harga barang
            PreparedStatement stmtHarga = conn.prepareStatement("SELECT harga FROM barang WHERE nama_barang = ?");
            stmtHarga.setString(1, barang);
            ResultSet rs = stmtHarga.executeQuery();
            rs.next();
            int harga = rs.getInt("harga");
            int totalHarga = harga * jumlah;

            txtTotalHarga.setText(String.valueOf(totalHarga)); // Update total harga di form

            // Mengupdate data transaksi
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE transaksi t " +
                "JOIN barang b ON t.id_barang = b.id_barang " +
                "JOIN konsumen k ON t.id_konsumen = k.id_konsumen " +
                "SET t.jumlah = ?, t.total_harga = ?, t.tanggal = ?, t.id_barang = b.id_barang, t.id_konsumen = k.id_konsumen " +
                "WHERE t.id_transaksi = ?"
            );
            stmt.setInt(1, jumlah);
            stmt.setInt(2, totalHarga);
            stmt.setString(3, tanggal);
            stmt.setInt(4, idTransaksi);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Data berhasil diubah!");
            tampilkanTransaksi();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void hapusTransaksi() {
        int selectedRow = tableTransaksi.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih transaksi yang ingin dihapus!");
            return;
        }

        int idTransaksi = (int) model.getValueAt(selectedRow, 0); // Ambil ID transaksi dari tabel

        int confirm = JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin menghapus data ini?", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/uas_crud", "root", "")) {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM transaksi WHERE id_transaksi = ?");
            stmt.setInt(1, idTransaksi);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Data berhasil dihapus!");
            tampilkanTransaksi();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void tampilkanTransaksi() {
        model.setRowCount(0);

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/uas_crud", "root", "");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT t.id_transaksi, b.nama_barang, k.nama_konsumen, t.jumlah, t.total_harga, t.tanggal " +
                 "FROM transaksi t JOIN barang b ON t.id_barang = b.id_barang " +
                 "JOIN konsumen k ON t.id_konsumen = k.id_konsumen"
             )) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id_transaksi"),
                    rs.getString("nama_barang"),
                    rs.getString("nama_konsumen"),
                    rs.getInt("jumlah"),
                    rs.getInt("total_harga"),
                    rs.getString("tanggal")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void isiFormDariTabel() {
        int selectedRow = tableTransaksi.getSelectedRow();
        if (selectedRow != -1) {
            cbBarang.setSelectedItem(model.getValueAt(selectedRow, 1).toString());
            cbKonsumen.setSelectedItem(model.getValueAt(selectedRow, 2).toString());
            txtJumlah.setText(model.getValueAt(selectedRow, 3).toString());
            txtTotalHarga.setText(model.getValueAt(selectedRow, 4).toString());
            txtTanggal.setText(model.getValueAt(selectedRow, 5).toString());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Transaksi().setVisible(true));
    }
}