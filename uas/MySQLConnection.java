package uas;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLConnection {
    public static void main(String[] args) {
        // URL koneksi database
        String url = "jdbc:mysql://localhost:3306/uas_crud"; // Nama database disesuaikan
        String username = "root"; // Username MySQL
        String password = ""; // Password MySQL

        Connection connection = null;

        try {
            // Load Driver MySQL (opsional untuk JDBC versi 4 ke atas)
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Membuat koneksi
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("Koneksi ke database uas_crud berhasil!");

        } catch (ClassNotFoundException e) {
            System.out.println("Driver JDBC tidak ditemukan!");
            e.printStackTrace();

        } catch (SQLException e) {
            System.out.println("Koneksi gagal!");
            e.printStackTrace();

        } finally {
            // Menutup koneksi
            if (connection != null) {
                try {
                    connection.close();
                    System.out.println("Koneksi berhasil ditutup.");
                } catch (SQLException e) {
                    System.out.println("Gagal menutup koneksi!");
                    e.printStackTrace();
                }
            }
        }
    }
}