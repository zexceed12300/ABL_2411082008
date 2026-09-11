package com.ihsan.produk.repository;

import com.ihsan.produk.entity.JenisProduk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JenisProdukRepository extends JpaRepository<JenisProduk, Integer> {
}
