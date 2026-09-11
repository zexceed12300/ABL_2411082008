package com.ihsan.produk.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "produk")
public class Produk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nama;
    private double harga;
    private String deskripsi;

    @Column(name = "jenis_produk_id")
    @JsonProperty("jenis_produk_id")
    private Integer jenisProdukId;

    public Produk() {
    }

    public Produk(Integer id, String nama, double harga, String deskripsi) {
        this.id = id;
        this.nama = nama;
        this.harga = harga;
        this.deskripsi = deskripsi;
    }

    public Produk(Integer id, String nama, double harga, String deskripsi, Integer jenisProdukId) {
        this.id = id;
        this.nama = nama;
        this.harga = harga;
        this.deskripsi = deskripsi;
        this.jenisProdukId = jenisProdukId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) { 
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public double getHarga() {
        return harga;
    }

    public void setHarga(double harga) {
        this.harga = harga;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    @JsonProperty("jenis_produk_id")
    public Integer getJenisProdukId() {
        return jenisProdukId;
    }

    @JsonProperty("jenis_produk_id")
    public void setJenisProdukId(Integer jenisProdukId) {
        this.jenisProdukId = jenisProdukId;
    }
}
