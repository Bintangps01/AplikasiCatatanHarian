import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class HapusCatatan extends Catatan {

    // Konstruktor untuk inisialisasi data catatan yang akan dihapus berdasarkan judul
    public HapusCatatan(String judul) {
        super(judul, null, null, null);  // Memanggil konstruktor superclass (Catatan) dengan judul yang akan dihapus
    }

    // Implementasi metode simpanCatatan untuk menghapus catatan berdasarkan judul
    // Menggunakan PreparedStatement untuk menghindari SQL Injection
    @Override
    public void simpanCatatan(Connection connection) throws SQLException {
        // SQL query untuk menghapus catatan berdasarkan judul
        String sql = "DELETE FROM catatan WHERE judul = ?";

        // Menyiapkan PreparedStatement dengan query yang telah disiapkan
        PreparedStatement preparedStatement = connection.prepareStatement(sql);

        // Menetapkan judul catatan yang akan dihapus
        preparedStatement.setString(1, this.judul);  // Mengatur judul yang akan dihapus

        // Mengeksekusi query untuk menghapus catatan
        int rowsDeleted = preparedStatement.executeUpdate();

        // Mengecek apakah ada catatan yang dihapus
        if (rowsDeleted > 0) {
            System.out.println("Catatan berhasil dihapus.");
        } else {
            // Jika tidak ada catatan yang dihapus (mungkin judul tidak ditemukan)
            System.out.println("Gagal menghapus catatan, pastikan judul yang dihapus ada.");
        }
    }
}
