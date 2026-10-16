package com.ihsan.product;

import com.ihsan.product.controller.ProductCategoryController;
import com.ihsan.product.controller.ProductController;
import com.ihsan.product.entity.Product;
import com.ihsan.product.entity.ProductCategory;
import com.ihsan.product.repository.ProductCategoryRepository;
import com.ihsan.product.repository.ProductRepository;
import com.ihsan.product.service.ProductCategoryService;
import com.ihsan.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProductApplicationTests {

	@Autowired
	private ProductController productController;

	@Autowired
	private ProductService productService;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private ProductCategoryController productCategoryController;

	@Autowired
	private ProductCategoryRepository productCategoryRepository;

	@BeforeEach
	void setUp() {
		productRepository.deleteAll();
		productCategoryRepository.deleteAll();
	}

	@Test
	void contextLoads() {
		assertNotNull(productController);
		assertNotNull(productService);
		assertNotNull(productRepository);
		assertNotNull(productCategoryController);
		assertNotNull(productCategoryRepository);
	}

	@Test
	void testCrudSuccessful() {
		// 1. CREATE PRODUCT (Save 2 new products)
		Product p1 = new Product(null, "Mouse Gaming", 150000.0, "Mouse RGB");
		ResponseEntity<Product> createResp1 = productController.createProduct(p1);
		assertEquals(HttpStatus.OK, createResp1.getStatusCode());
		assertNotNull(createResp1.getBody());
		assertNotNull(createResp1.getBody().getId());
		int id1 = createResp1.getBody().getId();
		assertEquals("Mouse Gaming", createResp1.getBody().getName());

		Product p2 = new Product(null, "Keyboard Mechanical", 450000.0, "Keyboard Blue Switch");
		ResponseEntity<Product> createResp2 = productController.createProduct(p2);
		assertEquals(HttpStatus.OK, createResp2.getStatusCode());
		assertNotNull(createResp2.getBody());
		int id2 = createResp2.getBody().getId();

		// 2. READ ALL (Should have 2 products)
		ResponseEntity<List<Product>> allResp = productController.getAllProduct(null);
		assertEquals(HttpStatus.OK, allResp.getStatusCode());
		assertNotNull(allResp.getBody());
		assertEquals(2, allResp.getBody().size());

		// 3. READ BY ID
		ResponseEntity<Product> getByIdResp = productController.getProductById(id1);
		assertEquals(HttpStatus.OK, getByIdResp.getStatusCode());
		assertNotNull(getByIdResp.getBody());
		assertEquals("Mouse Gaming", getByIdResp.getBody().getName());

		// 4. UPDATE (Update p1 to Wireless Mouse priced at 200000.0)
		Product updateData = new Product(null, "Mouse Wireless", 200000.0, "Mouse Tanpa Kabel");
		ResponseEntity<Product> updateResp = productController.updateProduct(id1, updateData);
		assertEquals(HttpStatus.OK, updateResp.getStatusCode());
		assertNotNull(updateResp.getBody());
		assertEquals("Mouse Wireless", updateResp.getBody().getName());
		assertEquals(200000.0, updateResp.getBody().getPrice());

		// 5. DELETE PRODUCT
		ResponseEntity<Void> deleteResp = productController.deleteProduct(id1);
		assertEquals(HttpStatus.NO_CONTENT, deleteResp.getStatusCode());

		// 6. VERIFY PRODUCT DELETED (Should be 404 NOT FOUND)
		ResponseEntity<Product> verifyDeleted = productController.getProductById(id1);
		assertEquals(HttpStatus.NOT_FOUND, verifyDeleted.getStatusCode());

		// Verify remaining products in database is 1 (Keyboard)
		ResponseEntity<List<Product>> remainingResp = productController.getAllProduct(null);
		assertEquals(HttpStatus.OK, remainingResp.getStatusCode());
		assertNotNull(remainingResp.getBody());
		assertEquals(1, remainingResp.getBody().size());
		assertEquals(id2, remainingResp.getBody().get(0).getId());
	}

	@Test
	void testProductCategoryCrudAndRelation() {
		// 1. CREATE PRODUCT CATEGORY
		ProductCategory pc1 = new ProductCategory(null, "Elektronik", "Barang elektronik");
		ResponseEntity<ProductCategory> createPcResp1 = productCategoryController.createProductCategory(pc1);
		assertEquals(HttpStatus.OK, createPcResp1.getStatusCode());
		assertNotNull(createPcResp1.getBody());
		assertNotNull(createPcResp1.getBody().getId());
		int pcId1 = createPcResp1.getBody().getId();
		assertEquals("Elektronik", createPcResp1.getBody().getName());

		ProductCategory pc2 = new ProductCategory(null, "Aksesoris", "Aksesoris komputer");
		ResponseEntity<ProductCategory> createPcResp2 = productCategoryController.createProductCategory(pc2);
		int pcId2 = createPcResp2.getBody().getId();

		// 2. READ ALL PRODUCT CATEGORIES
		ResponseEntity<List<ProductCategory>> allPcResp = productCategoryController.getAllProductCategory();
		assertEquals(HttpStatus.OK, allPcResp.getStatusCode());
		assertEquals(2, allPcResp.getBody().size());

		// 3. READ BY ID
		ResponseEntity<ProductCategory> getPcByIdResp = productCategoryController.getProductCategoryById(pcId1);
		assertEquals(HttpStatus.OK, getPcByIdResp.getStatusCode());
		assertEquals("Elektronik", getPcByIdResp.getBody().getName());

		// 4. UPDATE PRODUCT CATEGORY
		ProductCategory updatePcData = new ProductCategory(null, "Elektronik & Gadget", "Gadget terkini");
		ResponseEntity<ProductCategory> updatePcResp = productCategoryController.updateProductCategory(pcId1, updatePcData);
		assertEquals(HttpStatus.OK, updatePcResp.getStatusCode());
		assertEquals("Elektronik & Gadget", updatePcResp.getBody().getName());

		// 5. PRODUCT WITH PRODUCT_CATEGORY_ID (Without direct entity relation)
		Product relatedProduct = new Product(null, "Headset Gaming", 350000.0, "Headset 7.1 Surround", pcId1);
		ResponseEntity<Product> createProductResp = productController.createProduct(relatedProduct);
		assertEquals(HttpStatus.OK, createProductResp.getStatusCode());
		assertNotNull(createProductResp.getBody().getProductCategoryId());
		assertEquals(pcId1, createProductResp.getBody().getProductCategoryId());

		// 6. GET PRODUCT BY FILTER QUERY PARAM (?product-category-id={id})
		ResponseEntity<List<Product>> productByQueryParamResp = productController.getAllProduct(pcId1);
		assertEquals(HttpStatus.OK, productByQueryParamResp.getStatusCode());
		assertEquals(1, productByQueryParamResp.getBody().size());
		assertEquals("Headset Gaming", productByQueryParamResp.getBody().get(0).getName());

		// 7. DELETE PRODUCT FIRST THEN DELETE CATEGORY
		productController.deleteProduct(createProductResp.getBody().getId());
		ResponseEntity<Void> deletePcResp = productCategoryController.deleteProductCategory(pcId1);
		assertEquals(HttpStatus.NO_CONTENT, deletePcResp.getStatusCode());

		// 8. VERIFY CATEGORY DELETED
		ResponseEntity<ProductCategory> verifyDeletedPc = productCategoryController.getProductCategoryById(pcId1);
		assertEquals(HttpStatus.NOT_FOUND, verifyDeletedPc.getStatusCode());
	}
}
