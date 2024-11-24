// TambahCatatan.java
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

// Kelas untuk menambahkan catatan ke dalam database
// Kelas ini merupakan keturunan dari kelas Catatan dan mengimplementasikan metode simpanCatatan
public class TambahCatatan extends Catatan {

    // Konstruktor yang menginisialisasi objek TambahCatatan dengan data yang diberikan
    // Konstruktor ini akan memanggil konstruktor superclass (Catatan) untuk menginisialisasi data judul, isi, tanggal, dan kategori
    public TambahCatatan(String judul, String isi, String tanggal, String kategori) {
        super(judul, isi, tanggal, kategori);  // Panggil konstruktor superclass
    }

    // Implementasi metode simpanCatatan untuk menyimpan catatan ke database
    // Menggunakan PreparedStatement untuk menghindari SQL Injection dan meningkatkan keamanan
    @Override
    public void simpanCatatan(Connection connection) throws SQLException {
        // SQL query untuk memasukkan catatan baru ke dalam tabel 'catatan'
        String sql = "INSERT INTO catatan (judul, isi, tanggal, kategori) VALUES (?, ?, ?, ?)";
        
        // Menyiapkan PreparedStatement dengan query yang telah disiapkan
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        
        // Mengisi nilai untuk masing-masing parameter dalam query
        preparedStatement.setString(1, this.judul);      // Set judul catatan
        preparedStatement.setString(2, this.isi);        // Set isi catatan
        preparedStatement.setString(3, this.tanggal);    // Set tanggal catatan
        preparedStatement.setString(4, this.kategori);   // Set kategori catatan

        // Mengeksekusi query untuk menambahkan catatan ke dalam database
        preparedStatement.executeUpdate(); // Menjalankan perintah insert

        // Menampilkan pesan sukses setelah catatan berhasil disimpan
        System.out.println("Catatan berhasil ditambahkan.");
    }
}
