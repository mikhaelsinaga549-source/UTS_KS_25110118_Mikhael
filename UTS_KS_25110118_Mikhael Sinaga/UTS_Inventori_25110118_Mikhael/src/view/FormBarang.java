package view;

import dao.BarangDAO;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import model.Barang;

public class FormBarang extends JFrame {

    private final JTextField txtKode = new JTextField(20);
    private final JTextField txtNama = new JTextField(20);
    private final JTextField txtStok = new JTextField(20);
    private final JComboBox<String> cmbKategori =
        new JComboBox<>(new String[]{"Komputer", "Jaringan", "Alat Ukur"});
    private final JButton btnSimpan = new JButton("Simpan");
    private final JButton btnUbah = new JButton("Ubah");
    private final JButton btnHapus = new JButton("Hapus");
    private final JButton btnBersihkan = new JButton("Bersihkan");
    private final DefaultTableModel tableModel = new DefaultTableModel(
        new Object[]{"Kode", "Nama Barang", "Kategori", "Stok"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabel = new JTable(tableModel);
    private final BarangDAO dao = new BarangDAO();

    public FormBarang() {
        setTitle("Data Barang Laboratorium");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        initUI();
        pasangEvent();
        tampilData();
        pack();
        setMinimumSize(new Dimension(560, 480));
        setLocationRelativeTo(null);
    }

    private void initUI() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.anchor = GridBagConstraints.WEST;
        String[] label = {"Kode", "Nama", "Kategori", "Stok"};
        JComponent[] input = {txtKode, txtNama, cmbKategori, txtStok};
        for (int i = 0; i < label.length; i++) {
            g.gridx = 0; g.gridy = i; g.fill = GridBagConstraints.NONE; g.weightx = 0;
            form.add(new JLabel(label[i]), g);
            g.gridx = 1; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
            form.add(input[i], g);
        }
        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tombol.add(btnSimpan); tombol.add(btnUbah);
        tombol.add(btnHapus); tombol.add(btnBersihkan);
        g.gridx = 1; g.gridy = 4;
        form.add(tombol, g);

        JScrollPane sp = new JScrollPane(tabel);
        sp.setPreferredSize(new Dimension(540, 220));
        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        setLayout(new BorderLayout());
        add(form, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);
    }

    private void pasangEvent() {
        btnSimpan.addActionListener(e -> simpan());
        btnUbah.addActionListener(e -> ubah());
        btnHapus.addActionListener(e -> hapus());
        btnBersihkan.addActionListener(e -> bersihkan());
        tabel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { isiFormDariTabel(); }
        });
    }

    // Validasi input; mengembalikan Barang jika valid, null jika tidak
    private Barang ambilInput() {
        String kode = txtKode.getText().trim();
        String nama = txtNama.getText().trim();
        if (kode.isEmpty() || nama.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Kode dan nama tidak boleh kosong!",
                "Validasi", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        int stok;
        try {
            stok = Integer.parseInt(txtStok.getText().trim());
            if (stok < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Stok harus berupa angka >= 0!",
                "Validasi", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return new Barang(kode, nama, cmbKategori.getSelectedItem().toString(), stok);
    }

    private void simpan() {
        Barang b = ambilInput();
        if (b == null) return;
        if (dao.simpan(b)) {
            JOptionPane.showMessageDialog(this, "Data berhasil disimpan.");
            tampilData();
            bersihkan();
        } else {
            JOptionPane.showMessageDialog(this,
                "Data gagal disimpan (kode mungkin sudah ada atau koneksi bermasalah).",
                "Gagal", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ubah() {
        Barang b = ambilInput();
        if (b == null) return;
        if (dao.ubah(b)) {
            JOptionPane.showMessageDialog(this, "Data berhasil diubah.");
            tampilData();
            bersihkan();
        } else {
            JOptionPane.showMessageDialog(this,
                "Data gagal diubah. Pastikan kode barang sudah ada.",
                "Gagal", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void hapus() {
        String kode = txtKode.getText().trim();
        if (kode.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih data atau isi kode yang akan dihapus!",
                "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
            "Yakin ingin menghapus barang dengan kode " + kode + "?",
            "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        if (dao.hapus(kode)) {
            JOptionPane.showMessageDialog(this, "Data berhasil dihapus.");
            tampilData();
            bersihkan();
        } else {
            JOptionPane.showMessageDialog(this,
                "Data gagal dihapus. Pastikan kode barang ada.",
                "Gagal", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void bersihkan() {
        txtKode.setText("");
        txtNama.setText("");
        txtStok.setText("");
        cmbKategori.setSelectedIndex(0);
        tabel.clearSelection();
        txtKode.requestFocus();
    }

    private void tampilData() {
        tableModel.setRowCount(0);
        List<Barang> list = dao.getAll();
        for (Barang b : list) {
            tableModel.addRow(new Object[]{
                b.getKodeBarang(), b.getNamaBarang(), b.getKategori(), b.getStok()});
        }
    }

    private void isiFormDariTabel() {
        int row = tabel.getSelectedRow();
        if (row < 0) return;
        txtKode.setText(tableModel.getValueAt(row, 0).toString());
        txtNama.setText(tableModel.getValueAt(row, 1).toString());
        cmbKategori.setSelectedItem(tableModel.getValueAt(row, 2).toString());
        txtStok.setText(tableModel.getValueAt(row, 3).toString());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FormBarang().setVisible(true));
    }
}
