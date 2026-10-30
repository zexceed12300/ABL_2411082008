package com.ihsan.product.controller;

import com.ihsan.product.entity.Product;
import com.ihsan.product.middleware.RequireRole;
import com.ihsan.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProduct(
            @RequestParam(value = "product-category-id", required = false) Integer productCategoryId) {
        if (productCategoryId != null) {
            return ResponseEntity.ok(productService.getProductByCategoryId(productCategoryId));
        }
        return ResponseEntity.ok(productService.getAllProduct());
    }

    public ResponseEntity<List<Product>> getAllProduct() {
        return getAllProduct(null);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable("id") int id) {
        Product product = productService.getProductById(id);
        if (product != null) {
            return ResponseEntity.ok(product);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    @RequireRole("ADMIN")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product saved = productService.saveProduct(product);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<Product> updateProduct(@PathVariable("id") int id, @RequestBody Product product) {
        Product updated = productService.updateProduct(id, product);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<Void> deleteProduct(@PathVariable("id") int id) {
        boolean deleted = productService.deleteProduct(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
