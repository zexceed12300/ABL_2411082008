package com.ihsan.produk.service;

import com.ihsan.produk.entity.Produk;
import com.ihsan.produk.repository.ProdukRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service 
public class ProdukService {

    @Autowired
    private ProdukRepository produkRepository;

    // 1. Mengambil semua data produk
    public List<Produk> getAllProduk() {
        return produkRepository.findAll();
    }

    public List<Produk> getAllProduk(Integer jenisProdukId) {
        if (jenisProdukId != null) {
            return produkRepository.findByJenisProdukId(jenisProdukId);
        }
        return produkRepository.findAll();
    }

    // 2. Mengambil produk berdasarkan ID
    public Produk getProdukById(int id) {
        return produkRepository.findById(id).orElse(null);
    }

    // 3. Menyimpan produk baru
    public Produk saveProduk(Produk produk) {
        produk.setId(null); // Reset id agar database meng-generate ID baru secara otomatis dan mencegah 500 error jika client mengirim field id
        return produkRepository.save(produk);
    }

    // 4. Memperbarui data produk
    public Produk updateProduk(int id, Produk produk) {
        Produk produkAda = produkRepository.findById(id).orElse(null);
        if (produkAda != null) {
            produkAda.setNama(produk.getNama());
            produkAda.setHarga(produk.getHarga());
            produkAda.setDeskripsi(produk.getDeskripsi());
            if (produk.getJenisProdukId() != null) {
                produkAda.setJenisProdukId(produk.getJenisProdukId());
            }
            return produkRepository.save(produkAda);
        }
        return null;
    }

    // 5. Menghapus produk berdasarkan ID
    public boolean deleteProduk(int id) {
        if (produkRepository.existsById(id)) {
            produkRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // 6. Mengambil produk berdasarkan jenis produk ID
    public List<Produk> getProdukByJenisId(int jenisId) {
        return produkRepository.findByJenisProdukId(jenisId);
    }
}