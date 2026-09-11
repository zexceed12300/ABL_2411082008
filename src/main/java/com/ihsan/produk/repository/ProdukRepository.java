package com.ihsan.produk.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ihsan.produk.entity.Produk;

import java.util.List;

@Repository
public interface ProdukRepository extends JpaRepository<Produk, Integer> {

    List<Produk> findByJenisProdukId(Integer jenisProdukId);
}
