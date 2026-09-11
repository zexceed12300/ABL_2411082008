package com.ihsan.produk.service;

import com.ihsan.produk.entity.JenisProduk;
import com.ihsan.produk.repository.JenisProdukRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JenisProdukService {

    private final JenisProdukRepository jenisProdukRepository;

    @Autowired
    public JenisProdukService(JenisProdukRepository jenisProdukRepository) {
        this.jenisProdukRepository = jenisProdukRepository;
    }

    public List<JenisProduk> getAllJenisProduk() {
        return jenisProdukRepository.findAll();
    }

    public JenisProduk getJenisProdukById(int id) {
        return jenisProdukRepository.findById(id).orElse(null);
    }

    public JenisProduk saveJenisProduk(JenisProduk jenisProduk) {
        jenisProduk.setId(null); // Reset id agar auto-increment dan bebas error 500
        return jenisProdukRepository.save(jenisProduk);
    }

    public JenisProduk updateJenisProduk(int id, JenisProduk jenisProduk) {
        JenisProduk existing = jenisProdukRepository.findById(id).orElse(null);
        if (existing != null) {
            existing.setNama(jenisProduk.getNama());
            existing.setDeskripsi(jenisProduk.getDeskripsi());
            return jenisProdukRepository.save(existing);
        }
        return null;
    }

    public boolean deleteJenisProduk(int id) {
        if (jenisProdukRepository.existsById(id)) {
            jenisProdukRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
