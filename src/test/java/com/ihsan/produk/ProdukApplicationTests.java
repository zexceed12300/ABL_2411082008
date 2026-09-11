package com.ihsan.produk;

import com.ihsan.produk.controller.ProdukController;
import com.ihsan.produk.entity.Produk;
import com.ihsan.produk.repository.ProdukRepository;
import com.ihsan.produk.service.ProdukService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProdukApplicationTests {

	@Autowired
	private ProdukController produkController;

	@Autowired
	private ProdukService produkService;

	@Autowired
	private ProdukRepository produkRepository;

	@Autowired
	private com.ihsan.produk.controller.JenisProdukController jenisProdukController;

	@Autowired
	private com.ihsan.produk.repository.JenisProdukRepository jenisProdukRepository;

	@BeforeEach
	void setUp() {
		produkRepository.deleteAll();
		jenisProdukRepository.deleteAll();
	}

	@Test
	void contextLoads() {
		assertNotNull(produkController);
		assertNotNull(produkService);
		assertNotNull(produkRepository);
		assertNotNull(jenisProdukController);
		assertNotNull(jenisProdukRepository);
	}

	@Test
	void testCrudBerhasil() {
		// 1. CREATE PRODUK (Simpan 2 produk baru)
		Produk p1 = new Produk(null, "Mouse Gaming", 150000.0, "Mouse RGB");
		ResponseEntity<Produk> createResp1 = produkController.createProduk(p1);
		assertEquals(HttpStatus.OK, createResp1.getStatusCode());
		assertNotNull(createResp1.getBody());
		assertNotNull(createResp1.getBody().getId());
		int id1 = createResp1.getBody().getId();
		assertEquals("Mouse Gaming", createResp1.getBody().getNama());

		Produk p2 = new Produk(null, "Keyboard Mechanical", 450000.0, "Keyboard Blue Switch");
		ResponseEntity<Produk> createResp2 = produkController.createProduk(p2);
		assertEquals(HttpStatus.OK, createResp2.getStatusCode());
		assertNotNull(createResp2.getBody());
		int id2 = createResp2.getBody().getId();

		// 2. READ ALL (Harus ada 2 produk)
		ResponseEntity<List<Produk>> allResp = produkController.getAllProduk(null);
		assertEquals(HttpStatus.OK, allResp.getStatusCode());
		assertNotNull(allResp.getBody());
		assertEquals(2, allResp.getBody().size());

		// 3. READ BY ID
		ResponseEntity<Produk> getByIdResp = produkController.getProdukById(id1);
		assertEquals(HttpStatus.OK, getByIdResp.getStatusCode());
		assertNotNull(getByIdResp.getBody());
		assertEquals("Mouse Gaming", getByIdResp.getBody().getNama());

		// 4. UPDATE (Ubah p1 menjadi Mouse Wireless seharga 200000.0)
		Produk updateData = new Produk(null, "Mouse Wireless", 200000.0, "Mouse Tanpa Kabel");
		ResponseEntity<Produk> updateResp = produkController.updateProduk(id1, updateData);
		assertEquals(HttpStatus.OK, updateResp.getStatusCode());
		assertNotNull(updateResp.getBody());
		assertEquals("Mouse Wireless", updateResp.getBody().getNama());
		assertEquals(200000.0, updateResp.getBody().getHarga());

		// 5. DELETE PRODUK
		ResponseEntity<Void> deleteResp = produkController.deleteProduk(id1);
		assertEquals(HttpStatus.NO_CONTENT, deleteResp.getStatusCode());

		// 7. VERIFIKASI PRODUK TERHAPUS (Harus 404 NOT FOUND)
		ResponseEntity<Produk> verifyDeleted = produkController.getProdukById(id1);
		assertEquals(HttpStatus.NOT_FOUND, verifyDeleted.getStatusCode());

		// Verifikasi sisa produk di database tinggal 1 (yaitu Keyboard)
		ResponseEntity<List<Produk>> remainingResp = produkController.getAllProduk(null);
		assertEquals(HttpStatus.OK, remainingResp.getStatusCode());
		assertNotNull(remainingResp.getBody());
		assertEquals(1, remainingResp.getBody().size());
		assertEquals(id2, remainingResp.getBody().get(0).getId());
	}

	@Test
	void testJenisProdukCrudDanRelasi() {
		// 1. CREATE JENIS PRODUK
		com.ihsan.produk.entity.JenisProduk jp1 = new com.ihsan.produk.entity.JenisProduk(null, "Elektronik", "Barang elektronik");
		ResponseEntity<com.ihsan.produk.entity.JenisProduk> createJpResp1 = jenisProdukController.createJenisProduk(jp1);
		assertEquals(HttpStatus.OK, createJpResp1.getStatusCode());
		assertNotNull(createJpResp1.getBody());
		assertNotNull(createJpResp1.getBody().getId());
		int jpId1 = createJpResp1.getBody().getId();
		assertEquals("Elektronik", createJpResp1.getBody().getNama());

		com.ihsan.produk.entity.JenisProduk jp2 = new com.ihsan.produk.entity.JenisProduk(null, "Aksesoris", "Aksesoris komputer");
		ResponseEntity<com.ihsan.produk.entity.JenisProduk> createJpResp2 = jenisProdukController.createJenisProduk(jp2);
		int jpId2 = createJpResp2.getBody().getId();

		// 2. READ ALL JENIS PRODUK
		ResponseEntity<List<com.ihsan.produk.entity.JenisProduk>> allJpResp = jenisProdukController.getAllJenisProduk();
		assertEquals(HttpStatus.OK, allJpResp.getStatusCode());
		assertEquals(2, allJpResp.getBody().size());

		// 3. READ BY ID
		ResponseEntity<com.ihsan.produk.entity.JenisProduk> getJpByIdResp = jenisProdukController.getJenisProdukById(jpId1);
		assertEquals(HttpStatus.OK, getJpByIdResp.getStatusCode());
		assertEquals("Elektronik", getJpByIdResp.getBody().getNama());

		// 4. UPDATE JENIS PRODUK
		com.ihsan.produk.entity.JenisProduk updateJpData = new com.ihsan.produk.entity.JenisProduk(null, "Elektronik & Gadget", "Gadget terkini");
		ResponseEntity<com.ihsan.produk.entity.JenisProduk> updateJpResp = jenisProdukController.updateJenisProduk(jpId1, updateJpData);
		assertEquals(HttpStatus.OK, updateJpResp.getStatusCode());
		assertEquals("Elektronik & Gadget", updateJpResp.getBody().getNama());

		// 6. PRODUK DENGAN JENIS_PRODUK_ID (Tanpa relasi entity langsung)
		Produk produkRelasi = new Produk(null, "Headset Gaming", 350000.0, "Headset 7.1 Surround", jpId1);
		ResponseEntity<Produk> createProdukResp = produkController.createProduk(produkRelasi);
		assertEquals(HttpStatus.OK, createProdukResp.getStatusCode());
		assertNotNull(createProdukResp.getBody().getJenisProdukId());
		assertEquals(jpId1, createProdukResp.getBody().getJenisProdukId());

		// 7. GET PRODUK BERDASARKAN FILTER QUERY PARAM (?jenis-produk-id={id})
		ResponseEntity<List<Produk>> produkByQueryParamResp = produkController.getAllProduk(jpId1);
		assertEquals(HttpStatus.OK, produkByQueryParamResp.getStatusCode());
		assertEquals(1, produkByQueryParamResp.getBody().size());
		assertEquals("Headset Gaming", produkByQueryParamResp.getBody().get(0).getNama());

		// 8. HAPUS PRODUK DULU KEMUDIAN HAPUS JENIS PRODUK
		produkController.deleteProduk(createProdukResp.getBody().getId());
		ResponseEntity<Void> deleteJpResp = jenisProdukController.deleteJenisProduk(jpId1);
		assertEquals(HttpStatus.NO_CONTENT, deleteJpResp.getStatusCode());

		// 9. VERIFIKASI JENIS PRODUK TERHAPUS
		ResponseEntity<com.ihsan.produk.entity.JenisProduk> verifyDeletedJp = jenisProdukController.getJenisProdukById(jpId1);
		assertEquals(HttpStatus.NOT_FOUND, verifyDeletedJp.getStatusCode());
	}
}


