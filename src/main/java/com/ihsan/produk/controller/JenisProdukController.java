package com.ihsan.produk.controller;

import com.ihsan.produk.entity.JenisProduk;
import com.ihsan.produk.service.JenisProdukService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jenis-produk")
public class JenisProdukController {

    private final JenisProdukService jenisProdukService;

    @Autowired
    public JenisProdukController(JenisProdukService jenisProdukService) {
        this.jenisProdukService = jenisProdukService;
    }

    @GetMapping
    public ResponseEntity<List<JenisProduk>> getAllJenisProduk() {
        return ResponseEntity.ok(jenisProdukService.getAllJenisProduk());
    }

    @GetMapping("/{id}")
    public ResponseEntity<JenisProduk> getJenisProdukById(@PathVariable("id") int id) {
        JenisProduk jp = jenisProdukService.getJenisProdukById(id);
        if (jp != null) {
            return ResponseEntity.ok(jp);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<JenisProduk> createJenisProduk(@RequestBody JenisProduk jenisProduk) {
        JenisProduk saved = jenisProdukService.saveJenisProduk(jenisProduk);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<JenisProduk> updateJenisProduk(@PathVariable("id") int id, @RequestBody JenisProduk jenisProduk) {
        JenisProduk updated = jenisProdukService.updateJenisProduk(id, jenisProduk);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJenisProduk(@PathVariable("id") int id) {
        if (jenisProdukService.deleteJenisProduk(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
