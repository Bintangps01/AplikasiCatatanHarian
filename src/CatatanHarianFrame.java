import java.awt.Color;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.*;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Vector;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.json.JSONArray;
import org.json.JSONObject;
/**
 *
 * @author MyBook Z Series
 */
public class CatatanHarianFrame extends javax.swing.JFrame {

    private Connection connection; //Koneksi ke Database

    // Konstruktor untuk menginisialisasi komponen dan koneksi ke database
    public CatatanHarianFrame() {
        initComponents();
        initializeDatabase();  // Menginisialisasi koneksi ke database
        loadTableData();  // Memuat data catatan ke dalam tabel
        setupListeners();  // Mengatur listener untuk tombol-tombol
        setupTableSelectionListener();  // Mengatur listener untuk pemilihan tabel
        setupListSelectionListener();  // Mengatur listener untuk pemilihan di list
    }
    
    // Koneksi ke database
    private void initializeDatabase() {
        DatabaseConnection dbConnection = new DatabaseConnection();
        connection = dbConnection.getConnection();  // Mendapatkan koneksi database
        if (connection == null) {
            JOptionPane.showMessageDialog(this, "Gagal terhubung ke database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Memuat data catatan dari database ke tabel
    private void loadTableData() {
        try {
            DefaultTableModel tableModel = (DefaultTableModel) jTable1.getModel();
            tableModel.setRowCount(0);  // Reset data pada tabel sebelum mengisi data baru

            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT judul, isi, tanggal, kategori FROM catatan");

            while (rs.next()) {
                String judul = rs.getString("judul");
                String isi = rs.getString("isi");
                String tanggal = rs.getString("tanggal");
                String kategori = rs.getString("kategori");
                tableModel.addRow(new Object[]{judul, isi, tanggal, kategori});
            }
            rs.close();
            stmt.close();

            // Memperbarui JList setelah data dimuat ke tabel
            updateListCariFromTable();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + ex.getMessage());
        }
    }
    
    // Update list dari data tabel
    private void updateListCariFromTable() {
        DefaultListModel<String> listModel = new DefaultListModel<>();

        DefaultTableModel tableModel = (DefaultTableModel) jTable1.getModel();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String judul = (String) tableModel.getValueAt(i, 0);  // Ambil judul dari kolom pertama
            listModel.addElement(judul);  // Tambahkan judul ke list
        }

        listCari.setModel(listModel);  // Set model ke listCari
    }
    
    // Menambah Action Listener untuk tombol-tombol
    private void setupListeners() {
        buttonTambah.addActionListener(e -> tambahCatatan());  // Action listener untuk tombol tambah catatan
        buttonUbah.addActionListener(e -> ubahCatatan());  // Action listener untuk tombol ubah catatan
        buttonHapus.addActionListener(e -> hapusCatatan());  // Action listener untuk tombol hapus catatan
        buttonCari.addActionListener(e -> cariCatatan());  // Action listener untuk tombol cari catatan
        buttonImport.addActionListener(e -> importJSON());  // Action listener untuk tombol import data dari file JSON
        menuButtonImport.addActionListener(e -> importJSON());  // Action listener untuk menu import
        buttonExport.addActionListener(e -> exportJSON());  // Action listener untuk tombol ekspor data ke JSON
        menuButtonExport.addActionListener(e -> exportJSON());  // Action listener untuk menu ekspor
        temaNormal.addActionListener(e -> temaNormal());  // Action listener untuk tema normal
        temaPastel.addActionListener(e -> temaPastel());  // Action listener untuk tema pastel
        temaDark.addActionListener(e -> temaDark());  // Action listener untuk tema dark
        
        // Action listener untuk tombol cari catatan
        buttonCari.addActionListener(e -> {
            String searchQuery = fieldCari.getText().trim();
            String kategori = (String) comboKategoriCari.getSelectedItem();
            java.util.Date tanggalCari = dateTanggalCari.getDate();
            
            // Filter data berdasarkan input pencarian
            filterTableData(searchQuery, kategori, tanggalCari);
        });
    }
    
    private void tambahCatatan() {
    String judul = fieldJudulCatatan.getText().trim();  // Mendapatkan judul dari field
    String isi = fieldIsiCatatan.getText().trim();  // Mendapatkan isi dari field
    String kategori = comboKategori.getSelectedItem().toString();  // Mendapatkan kategori yang dipilih
    java.util.Date tanggalValue = dateTanggal.getDate();  // Mendapatkan tanggal dari date picker

    // Cek apakah ada field yang kosong
    if (judul.isEmpty() || isi.isEmpty() || kategori.isEmpty() || tanggalValue == null) {
        JOptionPane.showMessageDialog(this, "Semua field harus diisi.", "Peringatan", JOptionPane.WARNING_MESSAGE);
        return;  // Menghentikan proses jika ada field yang kosong
    }

    String tanggal = new SimpleDateFormat("yyyy-MM-dd").format(tanggalValue);

    // Membuat objek Catatan untuk menyimpan data
    Catatan catatan = new TambahCatatan(judul, isi, tanggal, kategori);
    try {
        catatan.simpanCatatan(connection);  // Menyimpan catatan ke database
        loadTableData();  // Memuat ulang data ke dalam tabel
        clearForm();  // Mengosongkan form setelah penambahan
        JOptionPane.showMessageDialog(this, "Catatan berhasil ditambahkan.", "Informasi", JOptionPane.INFORMATION_MESSAGE);
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
    }
}
    
    // Mengubah catatan yang sudah ada
    private void ubahCatatan() {
        int selectedRow = jTable1.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih catatan yang ingin diubah.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Mendapatkan nilai baru dari form input
        String judul = fieldJudulCatatan.getText().trim();
        String isi = fieldIsiCatatan.getText().trim();
        String kategori = comboKategori.getSelectedItem().toString();

        if (judul.isEmpty() || isi.isEmpty() || kategori.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        java.util.Date tanggalValue = dateTanggal.getDate();
        if (tanggalValue == null) {
            JOptionPane.showMessageDialog(this, "Tanggal harus diisi.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String tanggal = new SimpleDateFormat("yyyy-MM-dd").format(tanggalValue);

        // Mendapatkan judul lama dari baris yang dipilih di tabel
        String oldJudul = (String) jTable1.getValueAt(selectedRow, 0);

        // Membuat objek UbahCatatan dengan judul lama dan nilai baru
        UbahCatatan ubahCatatan = new UbahCatatan(oldJudul, judul, isi, tanggal, kategori);

        try {
            // Memanggil metode simpanCatatan untuk memperbarui catatan
            ubahCatatan.simpanCatatan(connection);

            // Memuat ulang tabel dan mengosongkan form setelah mengubah catatan
            loadTableData();
            clearForm();
            JOptionPane.showMessageDialog(this, "Catatan berhasil diubah.", "Informasi", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat mengubah catatan.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Menghapus catatan
    private void hapusCatatan() {
        int selectedRow = jTable1.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih catatan yang ingin dihapus.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String judul = (String) jTable1.getValueAt(selectedRow, 0);
        Catatan catatan = new HapusCatatan(judul);
        try {
            catatan.simpanCatatan(connection);  // Menghapus catatan dari database
            loadTableData();  // Memuat ulang data tabel
            clearForm();  // Mengosongkan form input
            JOptionPane.showMessageDialog(this, "Catatan berhasil dihapus.", "Informasi", JOptionPane.INFORMATION_MESSAGE); //Pesan berhasil hapus
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    // Mencari catatan dan menampilkannya ke tabel
    private void cariCatatan() {
        String cari = fieldCari.getText().trim();
        String kategori = comboKategoriCari.getSelectedItem().toString();
        java.util.Date tanggalCariValue = dateTanggalCari.getDate();

        try {
            String sql = "SELECT judul, isi, tanggal, kategori FROM catatan WHERE judul LIKE ?";
            if (!kategori.equals("Semua")) {
                sql += " AND kategori = ?";
            }
            if (tanggalCariValue != null) {
                sql += " AND tanggal = ?";
            }

            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, "%" + cari + "%");

            int paramIndex = 2; // Mulai dari indeks kedua untuk parameter
            if (!kategori.equals("Semua")) {
                preparedStatement.setString(paramIndex++, kategori);
            }
            if (tanggalCariValue != null) {
                String tanggalCari = new java.text.SimpleDateFormat("yyyy-MM-dd").format(tanggalCariValue);
                preparedStatement.setString(paramIndex, tanggalCari);
            }

            ResultSet resultSet = preparedStatement.executeQuery();

            DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
            model.setRowCount(0);

            while (resultSet.next()) {
                Vector<String> row = new Vector<>();
                row.add(resultSet.getString("judul"));
                row.add(resultSet.getString("isi"));
                row.add(resultSet.getString("tanggal"));
                row.add(resultSet.getString("kategori"));
                model.addRow(row);
            }
            
            // Memperbarui JList setelah data dimuat ke tabel
            updateListCariFromTable();

            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error saat mencari data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    
    // Menyaring data berdasarkan pencarian, kategori, dan tanggal
    private void filterTableData(String searchQuery, String kategori, java.util.Date tanggalCari) {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        DefaultListModel<String> listModel = new DefaultListModel<>();

        // Menghapus semua data yang ada di JList sebelum memperbarui
        listModel.clear();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String tanggalFilter = tanggalCari != null ? dateFormat.format(tanggalCari) : null;

        for (int i = 0; i < model.getRowCount(); i++) {
            String judul = (String) model.getValueAt(i, 0);
            String isi = (String) model.getValueAt(i, 1);
            String tanggal = (String) model.getValueAt(i, 2);
            String kat = (String) model.getValueAt(i, 3);

            // Filter berdasarkan kategori, judul, isi, dan tanggal
            boolean matchKategori = kategori.equals("Semua") || kat.equals(kategori);
            boolean matchJudulIsi = judul.toLowerCase().contains(searchQuery.toLowerCase()) || isi.toLowerCase().contains(searchQuery.toLowerCase());
            boolean matchTanggal = (tanggalFilter == null) || tanggal.equals(tanggalFilter);

            if (matchKategori && matchJudulIsi && matchTanggal) {
                listModel.addElement(judul); // Tambahkan judul ke JList jika cocok
            }
        }

        // Set model baru ke JList
        listCari.setModel(listModel);
    }
    
    //Mengimpor Catatan dari file JSON
    private void importJSON() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            try {
                // Membaca file JSON
                String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
                JSONArray jsonArray = new JSONArray(content); // Parsing JSON ke dalam JSONArray

                // Proses setiap objek JSON di dalam array
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    // Ambil data dari objek JSON
                    String judul = jsonObject.getString("judul").trim();
                    String isi = jsonObject.getString("isi").trim();
                    String tanggal = jsonObject.getString("tanggal").trim();
                    String kategori = jsonObject.getString("kategori").trim();

                    // Validasi tanggal
                    try {
                        new SimpleDateFormat("yyyy-MM-dd").parse(tanggal); // Pastikan formatnya valid
                    } catch (ParseException e) {
                        JOptionPane.showMessageDialog(this, 
                            "Format tanggal tidak valid pada objek: " + jsonObject.toString(), 
                            "Error", JOptionPane.ERROR_MESSAGE);
                        continue; // Lewati objek jika tanggal salah
                    }

                    // Simpan ke database
                    String sql = "INSERT INTO catatan (judul, isi, tanggal, kategori) VALUES (?, ?, ?, ?)";
                    PreparedStatement preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.setString(1, judul);
                    preparedStatement.setString(2, isi);
                    preparedStatement.setString(3, tanggal);
                    preparedStatement.setString(4, kategori);
                    preparedStatement.executeUpdate();
                }

                loadTableData(); // Muat ulang data ke tabel
                JOptionPane.showMessageDialog(this, "Data berhasil diimpor.", "Informasi", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Gagal mengimpor data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // Mengekspor catatan ke dalam file JSON
    private void exportJSON() {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT judul, isi, tanggal, kategori FROM catatan");

            JSONArray jsonArray = new JSONArray();

            while (rs.next()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("judul", rs.getString("judul"));
                jsonObject.put("isi", rs.getString("isi"));
                jsonObject.put("tanggal", rs.getString("tanggal"));
                jsonObject.put("kategori", rs.getString("kategori"));
                jsonArray.put(jsonObject);
            }
            
            rs.close();
            stmt.close();

            // Memilih file untuk menyimpan data
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Simpan File JSON");
            int returnValue = fileChooser.showSaveDialog(this);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                    writer.write(jsonArray.toString(4));  // Menyimpan JSON dengan indentasi 4 spasi
                    JOptionPane.showMessageDialog(this, "Data berhasil diekspor.");
                }
            }
        } catch (SQLException | IOException e) {
            JOptionPane.showMessageDialog(this, "Error exporting data: " + e.getMessage());
        }
    }
    
    // Setup listener untuk pemilihan di tabel
    private void setupTableSelectionListener() {
        jTable1.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = jTable1.getSelectedRow();
                if (selectedRow != -1) {
                    String judul = (String) jTable1.getValueAt(selectedRow, 0);
                    String isi = (String) jTable1.getValueAt(selectedRow, 1);
                    String tanggal = (String) jTable1.getValueAt(selectedRow, 2);
                    String kategori = (String) jTable1.getValueAt(selectedRow, 3);

                    fieldJudulCatatan.setText(judul);
                    fieldIsiCatatan.setText(isi);
                    comboKategori.setSelectedItem(kategori);

                    try {
                        java.util.Date dateValue = new SimpleDateFormat("yyyy-MM-dd").parse(tanggal);
                        dateTanggal.setDate(dateValue);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }
    // Setup listener untuk pemilihan di list pencarian
    private void setupListSelectionListener() {
    listCari.addListSelectionListener(e -> {
        if (!e.getValueIsAdjusting()) {
            String selectedJudul = listCari.getSelectedValue();
            if (selectedJudul != null) {
                // Cari catatan di tabel berdasarkan judul
                DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
                for (int i = 0; i < model.getRowCount(); i++) {
                    String judul = (String) model.getValueAt(i, 0);
                    if (judul.equals(selectedJudul)) {
                        // Pilih baris yang sesuai di jTable1
                        jTable1.setRowSelectionInterval(i, i);

                        // Set data ke form
                        String isi = (String) model.getValueAt(i, 1);
                        String tanggal = (String) model.getValueAt(i, 2);
                        String kategori = (String) model.getValueAt(i, 3);

                        fieldJudulCatatan.setText(judul);
                        fieldIsiCatatan.setText(isi);
                        comboKategori.setSelectedItem(kategori);

                        try {
                            // Format tanggal dari string ke Date dan set ke komponen dateTanggal
                            java.util.Date dateValue = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(tanggal);
                            dateTanggal.setDate(dateValue);
                        } catch (java.text.ParseException ex) {
                            ex.printStackTrace();
                        }

                        break;
                    }
                }
            }
        }
    });
}

    
    //Membersihkan form input
    private void clearForm() {
        fieldJudulCatatan.setText("");
        fieldIsiCatatan.setText("");
        comboKategori.setSelectedIndex(0);
        dateTanggal.setDate(null);
    }
    
    //Mengatur Tema Normal
    private void temaNormal() {                                           
        panelUtama.setBackground(new Color(102, 102, 255));
        panelCRUD.setBackground(new Color(255, 255, 51));
        panelPencarian.setBackground(new Color(255, 255, 51));
        panelTabel.setBackground(new Color(255, 255, 51));
        panelButtonCRUD.setBackground(new Color(255, 255, 51));
        panelButtonIE.setBackground(new Color(255, 255, 51));
        dateTanggal.setBackground(new Color(255, 255, 51));
        dateTanggalCari.setBackground(new Color(255, 255, 51));
        labelTitle.setForeground(Color.black);
        labelJudul.setForeground(Color.black);
        labelKategori.setForeground(Color.black);
        labelCatatan.setForeground(Color.black);
        labelTanggal.setForeground(Color.black);
        labelPencarian.setForeground(Color.black);
    }                                          

    //Mengatur Tema Pastel
    private void temaPastel() {                                           
        panelUtama.setBackground(new Color(135, 206, 235));  // Light Sky Blue
        panelCRUD.setBackground(new Color(255, 182, 193));  // Light Pink
        panelPencarian.setBackground(new Color(255, 182, 193));  // Light Pink
        panelTabel.setBackground(new Color(255, 182, 193));  // Light Pink
        panelButtonCRUD.setBackground(new Color(255, 182, 193));  // Light Pink
        panelButtonIE.setBackground(new Color(255, 182, 193));  // Light Pink
        dateTanggal.setBackground(new Color(255, 182, 193));
        dateTanggalCari.setBackground(new Color(255, 182, 193));
        labelTitle.setForeground(Color.black);  // Black
        labelJudul.setForeground(Color.black);  // Black
        labelKategori.setForeground(Color.black);  // Black
        labelCatatan.setForeground(Color.black);  // Black
        labelTanggal.setForeground(Color.black);  // Black
        labelPencarian.setForeground(Color.black);  // Black
    }                                          

    //Mengatur Tema Dark
    private void temaDark() {                                         
        panelUtama.setBackground(Color.black);
        panelCRUD.setBackground(Color.darkGray);
        panelPencarian.setBackground(Color.darkGray);
        panelTabel.setBackground(Color.darkGray);
        panelButtonCRUD.setBackground(Color.darkGray);
        panelButtonIE.setBackground(Color.darkGray);
        dateTanggal.setBackground(Color.darkGray);
        dateTanggalCari.setBackground(Color.darkGray);
        labelTitle.setForeground(Color.white);
        labelJudul.setForeground(Color.white);
        labelKategori.setForeground(Color.white);
        labelCatatan.setForeground(Color.white);
        labelTanggal.setForeground(Color.white);
        labelPencarian.setForeground(Color.white);
    }                        
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        JOptionPane = new javax.swing.JOptionPane();
        panelUtama = new javax.swing.JPanel();
        labelTitle = new javax.swing.JLabel();
        panelCRUD = new javax.swing.JPanel();
        labelJudul = new javax.swing.JLabel();
        labelCatatan = new javax.swing.JLabel();
        labelKategori = new javax.swing.JLabel();
        fieldJudulCatatan = new javax.swing.JTextField();
        comboKategori = new javax.swing.JComboBox<>();
        panelButtonCRUD = new javax.swing.JPanel();
        buttonTambah = new javax.swing.JButton();
        buttonUbah = new javax.swing.JButton();
        buttonHapus = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        fieldIsiCatatan = new javax.swing.JTextArea();
        dateTanggal = new com.toedter.calendar.JDateChooser();
        labelTanggal = new javax.swing.JLabel();
        panelPencarian = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        listCari = new javax.swing.JList<>();
        fieldCari = new javax.swing.JTextField();
        labelPencarian = new javax.swing.JLabel();
        buttonCari = new javax.swing.JButton();
        comboKategoriCari = new javax.swing.JComboBox<>();
        dateTanggalCari = new com.toedter.calendar.JDateChooser();
        panelTabel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        panelButtonIE = new javax.swing.JPanel();
        buttonImport = new javax.swing.JButton();
        buttonExport = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        menuButtonImport = new javax.swing.JMenuItem();
        menuButtonExport = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenu3 = new javax.swing.JMenu();
        temaNormal = new javax.swing.JMenuItem();
        temaPastel = new javax.swing.JMenuItem();
        temaDark = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        panelUtama.setBackground(new java.awt.Color(102, 102, 255));
        panelUtama.setLayout(new java.awt.GridBagLayout());

        labelTitle.setFont(new java.awt.Font("Segoe UI Variable", 1, 18)); // NOI18N
        labelTitle.setText("APLIKASI CATATAN HARIAN");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        panelUtama.add(labelTitle, gridBagConstraints);

        panelCRUD.setBackground(new java.awt.Color(255, 255, 51));
        panelCRUD.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        panelCRUD.setLayout(new java.awt.GridBagLayout());

        labelJudul.setText("Judul");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(7, 20, 7, 20);
        panelCRUD.add(labelJudul, gridBagConstraints);

        labelCatatan.setText("Catatan");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(7, 20, 7, 20);
        panelCRUD.add(labelCatatan, gridBagConstraints);

        labelKategori.setText("Kategori");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(7, 20, 7, 20);
        panelCRUD.add(labelKategori, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 400;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(7, 20, 7, 20);
        panelCRUD.add(fieldJudulCatatan, gridBagConstraints);

        comboKategori.setBackground(new java.awt.Color(240, 240, 240));
        comboKategori.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Catatan Biasa", "Pekerjaan", "Pendidikan", "Kesehatan" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(7, 20, 7, 20);
        panelCRUD.add(comboKategori, gridBagConstraints);

        panelButtonCRUD.setBackground(new java.awt.Color(255, 255, 51));
        panelButtonCRUD.setLayout(new java.awt.GridBagLayout());

        buttonTambah.setText("Tambah");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        panelButtonCRUD.add(buttonTambah, gridBagConstraints);

        buttonUbah.setText("Ubah");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        panelButtonCRUD.add(buttonUbah, gridBagConstraints);

        buttonHapus.setText("Hapus");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        panelButtonCRUD.add(buttonHapus, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new java.awt.Insets(7, 0, 7, 0);
        panelCRUD.add(panelButtonCRUD, gridBagConstraints);

        fieldIsiCatatan.setColumns(20);
        fieldIsiCatatan.setRows(5);
        jScrollPane3.setViewportView(fieldIsiCatatan);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.ipadx = 400;
        gridBagConstraints.ipady = 200;
        gridBagConstraints.insets = new java.awt.Insets(7, 20, 7, 20);
        panelCRUD.add(jScrollPane3, gridBagConstraints);

        dateTanggal.setBackground(new java.awt.Color(255, 255, 51));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(7, 20, 7, 20);
        panelCRUD.add(dateTanggal, gridBagConstraints);

        labelTanggal.setText("Tanggal");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        panelCRUD.add(labelTanggal, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panelUtama.add(panelCRUD, gridBagConstraints);

        panelPencarian.setBackground(new java.awt.Color(255, 255, 51));
        panelPencarian.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        panelPencarian.setLayout(new java.awt.GridBagLayout());

        jScrollPane2.setViewportView(listCari);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipady = 500;
        gridBagConstraints.insets = new java.awt.Insets(0, 18, 0, 20);
        panelPencarian.add(jScrollPane2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 150;
        gridBagConstraints.insets = new java.awt.Insets(0, 18, 0, 0);
        panelPencarian.add(fieldCari, gridBagConstraints);

        labelPencarian.setFont(new java.awt.Font("Segoe UI Variable", 1, 14)); // NOI18N
        labelPencarian.setText("Pencarian");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 18, 0, 20);
        panelPencarian.add(labelPencarian, gridBagConstraints);

        buttonCari.setText("Cari");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 20);
        panelPencarian.add(buttonCari, gridBagConstraints);

        comboKategoriCari.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Semua", "Catatan Biasa", "Pekerjaan", "Pendidikan", "Kesehatan" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 18, 0, 20);
        panelPencarian.add(comboKategoriCari, gridBagConstraints);

        dateTanggalCari.setBackground(new java.awt.Color(255, 255, 51));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 18, 0, 20);
        panelPencarian.add(dateTanggalCari, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panelUtama.add(panelPencarian, gridBagConstraints);

        panelTabel.setBackground(new java.awt.Color(255, 255, 51));
        panelTabel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        panelTabel.setLayout(new java.awt.GridBagLayout());

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Judul", "Isi", "Tanggal", "Kategori"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(jTable1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = -429;
        gridBagConstraints.ipady = -402;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        panelTabel.add(jScrollPane1, gridBagConstraints);

        panelButtonIE.setBackground(new java.awt.Color(255, 255, 51));

        buttonImport.setText("Import");
        panelButtonIE.add(buttonImport);

        buttonExport.setText("Export");
        panelButtonIE.add(buttonExport);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        panelTabel.add(panelButtonIE, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 200;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panelUtama.add(panelTabel, gridBagConstraints);

        getContentPane().add(panelUtama, java.awt.BorderLayout.CENTER);

        jMenu1.setText("File");

        menuButtonImport.setText("Import Data");
        jMenu1.add(menuButtonImport);

        menuButtonExport.setText("Export Data");
        jMenu1.add(menuButtonExport);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Pengaturan");

        jMenu3.setText("Tema Aplikasi");

        temaNormal.setText("Normal");
        jMenu3.add(temaNormal);

        temaPastel.setText("Pastel");
        jMenu3.add(temaPastel);

        temaDark.setText("Dark");
        jMenu3.add(temaDark);

        jMenu2.add(jMenu3);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(CatatanHarianFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(CatatanHarianFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(CatatanHarianFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CatatanHarianFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new CatatanHarianFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JOptionPane JOptionPane;
    private javax.swing.JButton buttonCari;
    private javax.swing.JButton buttonExport;
    private javax.swing.JButton buttonHapus;
    private javax.swing.JButton buttonImport;
    private javax.swing.JButton buttonTambah;
    private javax.swing.JButton buttonUbah;
    private javax.swing.JComboBox<String> comboKategori;
    private javax.swing.JComboBox<String> comboKategoriCari;
    private com.toedter.calendar.JDateChooser dateTanggal;
    private com.toedter.calendar.JDateChooser dateTanggalCari;
    private javax.swing.JTextField fieldCari;
    private javax.swing.JTextArea fieldIsiCatatan;
    private javax.swing.JTextField fieldJudulCatatan;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel labelCatatan;
    private javax.swing.JLabel labelJudul;
    private javax.swing.JLabel labelKategori;
    private javax.swing.JLabel labelPencarian;
    private javax.swing.JLabel labelTanggal;
    private javax.swing.JLabel labelTitle;
    private javax.swing.JList<String> listCari;
    private javax.swing.JMenuItem menuButtonExport;
    private javax.swing.JMenuItem menuButtonImport;
    private javax.swing.JPanel panelButtonCRUD;
    private javax.swing.JPanel panelButtonIE;
    private javax.swing.JPanel panelCRUD;
    private javax.swing.JPanel panelPencarian;
    private javax.swing.JPanel panelTabel;
    private javax.swing.JPanel panelUtama;
    private javax.swing.JMenuItem temaDark;
    private javax.swing.JMenuItem temaNormal;
    private javax.swing.JMenuItem temaPastel;
    // End of variables declaration//GEN-END:variables
}
