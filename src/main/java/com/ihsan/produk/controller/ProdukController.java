package com.ihsan.produk.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import org.springframework.http.ResponseEntity;
import com.ihsan.produk.entity.Produk;

@RestController
@RequestMapping("/api/produk")
public class ProdukController {

    @GetMapping
    public ResponseEntity<List<Produk>> getAllProduk() {
        List<Produk> produkList = List.of(
            new Produk(1, "Mouse", 10000, "Mouse gaming"),
            new Produk(2, "Keyboard", 20000, "Keyboard mechanical")
        );
        return ResponseEntity.ok(produkList);
    } 

    @GetMapping("/{id}")
    public ResponseEntity<Produk> getProdukById(@PathVariable("id") int id) {
        if (id==1) {
            return ResponseEntity.ok(
                new Produk(1, "Mouse", 10000, "Mouse gaming")
            );
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Produk> createProduk(@RequestBody Produk produk) {
        return ResponseEntity.ok(produk);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Produk> updateProduk(@PathVariable("id") int id, @RequestBody Produk produk) {
        if (id==1) {
            produk.setId(id);
            return ResponseEntity.ok(produk);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduk(@PathVariable("id") int id) {
        if (id==1) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
