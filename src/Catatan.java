import java.sql.Connection;
import java.sql.SQLException;

// Kelas dasar untuk semua jenis catatan
// Kelas ini adalah kelas abstrak, yang berarti tidak bisa langsung dibuat objeknya,
// tetapi harus di-extend oleh subclass untuk implementasi metode simpanCatatan.
abstract class Catatan {
    protected String judul;   // Menyimpan judul catatan
    protected String isi;     // Menyimpan isi catatan
    protected String tanggal; // Menyimpan tanggal catatan
    protected String kategori;// Menyimpan kategori catatan

    // Konstruktor untuk menginisialisasi objek Catatan dengan data yang diberikan
    public Catatan(String judul, String isi, String tanggal, String kategori) {
        this.judul = judul;     // Inisialisasi judul
        this.isi = isi;         // Inisialisasi isi
        this.tanggal = tanggal; // Inisialisasi tanggal
        this.kategori = kategori; // Inisialisasi kategori
    }

    // Metode abstrak untuk menyimpan catatan ke database
    public abstract void simpanCatatan(Connection connection) throws SQLException;
}
