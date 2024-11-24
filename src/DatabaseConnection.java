import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private Connection connection;  // Menyimpan koneksi ke database

    // Konstruktor untuk menghubungkan ke database dan membuat tabel jika belum ada
    public DatabaseConnection() {
        connectDatabase();  // Memanggil metode untuk menghubungkan ke database
    }

    // Metode untuk menghubungkan ke database dan membuat tabel 'catatan' jika belum ada
    private void connectDatabase() {
        try {
            // Menghubungkan ke database SQLite yang bernama catatan.db
            connection = DriverManager.getConnection("jdbc:sqlite:catatan.db");

            // Membuat statement untuk menjalankan perintah SQL
            Statement statement = connection.createStatement();

            // Membuat tabel 'catatan' jika tabel tersebut belum ada
            statement.execute("CREATE TABLE IF NOT EXISTS catatan (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +  // ID unik yang bertambah otomatis
                "judul TEXT NOT NULL, " +  // Judul catatan yang tidak boleh kosong
                "isi TEXT NOT NULL, " +  // Isi catatan yang tidak boleh kosong
                "tanggal DATE NOT NULL, " +  // Tanggal catatan yang tidak boleh kosong
                "kategori TEXT NOT NULL)");  // Kategori catatan yang tidak boleh kosong

            // Menutup statement setelah perintah SQL selesai dijalankan
            statement.close();
        } catch (SQLException e) {
            // Menangani exception jika ada kesalahan saat menghubungkan ke database atau menjalankan SQL
            e.printStackTrace();
        }
    }

    // Mengembalikan objek koneksi ke database
    public Connection getConnection() {
        return connection;
    }
}