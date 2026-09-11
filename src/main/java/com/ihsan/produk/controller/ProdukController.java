package com.ihsan.produk.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import org.springframework.http.ResponseEntity;
import com.ihsan.produk.entity.Produk;
import com.ihsan.produk.service.ProdukService;

@RestController
@RequestMapping("/api/produk")
public class ProdukController {

    private final ProdukService produkService;

    @Autowired
    public ProdukController(ProdukService produkService) {
        this.produkService = produkService;
    }

    @GetMapping
    public ResponseEntity<List<Produk>> getAllProduk(
            @RequestParam(value = "jenis-produk-id", required = false) Integer jenisProdukId) {
        if (jenisProdukId != null) {
            return ResponseEntity.ok(produkService.getProdukByJenisId(jenisProdukId));
        }
        return ResponseEntity.ok(produkService.getAllProduk());
    }

    public ResponseEntity<List<Produk>> getAllProduk() {
        return getAllProduk(null);
    } 

    @GetMapping("/{id}")
    public ResponseEntity<Produk> getProdukById(@PathVariable("id") int id) {
        Produk produk = produkService.getProdukById(id);
        if (produk != null) {
            return ResponseEntity.ok(produk);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Produk> createProduk(@RequestBody Produk produk) {
        Produk saved = produkService.saveProduk(produk);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Produk> updateProduk(@PathVariable("id") int id, @RequestBody Produk produk) {
        Produk updated = produkService.updateProduk(id, produk);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduk(@PathVariable("id") int id) {
        boolean deleted = produkService.deleteProduk(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

