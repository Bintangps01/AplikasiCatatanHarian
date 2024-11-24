import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UbahCatatan extends Catatan {
    private String oldJudul;  // Menyimpan judul lama untuk mengidentifikasi catatan yang akan diubah

    // Konstruktor untuk inisialisasi data catatan baru serta judul lama untuk identifikasi catatan yang akan diperbarui
    public UbahCatatan(String oldJudul, String judul, String isi, String tanggal, String kategori) {
        super(judul, isi, tanggal, kategori); // Memanggil konstruktor superclass (Catatan) untuk menginisialisasi judul, isi, tanggal, dan kategori
        this.oldJudul = oldJudul;  // Menyimpan judul lama untuk digunakan sebagai identifier pada pembaruan data
    }

    // Implementasi metode simpanCatatan untuk memperbarui catatan dalam database
    // Menggunakan PreparedStatement untuk menghindari SQL Injection
    @Override
    public void simpanCatatan(Connection connection) throws SQLException {
        // SQL query untuk memperbarui catatan yang ada
        String sql = "UPDATE catatan SET judul = ?, isi = ?, tanggal = ?, kategori = ? WHERE judul = ?";

        // Menyiapkan PreparedStatement dengan query yang telah disiapkan
        PreparedStatement preparedStatement = connection.prepareStatement(sql);

        // Menetapkan nilai baru untuk parameter yang akan diperbarui
        preparedStatement.setString(1, this.judul);      // Mengatur nilai judul baru
        preparedStatement.setString(2, this.isi);        // Mengatur isi catatan yang baru
        preparedStatement.setString(3, this.tanggal);    // Mengatur tanggal catatan yang baru
        preparedStatement.setString(4, this.kategori);   // Mengatur kategori catatan yang baru
        preparedStatement.setString(5, this.oldJudul);   // Menggunakan judul lama sebagai acuan untuk catatan yang akan diperbarui

        // Mengeksekusi query untuk memperbarui catatan
        int rowsUpdated = preparedStatement.executeUpdate();

        // Mengecek apakah ada catatan yang diperbarui
        if (rowsUpdated > 0) {
            System.out.println("Catatan berhasil diubah.");
        } else {
            // Jika tidak ada catatan yang diperbarui (mungkin judul lama tidak ada)
            System.out.println("Gagal mengubah catatan, pastikan judul yang diubah ada.");
        }
    }
}
