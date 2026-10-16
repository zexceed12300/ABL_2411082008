package com.ihsan.product.controller;

import com.ihsan.product.entity.ProductCategory;
import com.ihsan.product.service.ProductCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-category")
public class ProductCategoryController {

    private final ProductCategoryService productCategoryService;

    @Autowired
    public ProductCategoryController(ProductCategoryService productCategoryService) {
        this.productCategoryService = productCategoryService;
    }

    @GetMapping
    public ResponseEntity<List<ProductCategory>> getAllProductCategory() {
        return ResponseEntity.ok(productCategoryService.getAllProductCategory());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductCategory> getProductCategoryById(@PathVariable("id") int id) {
        ProductCategory pc = productCategoryService.getProductCategoryById(id);
        if (pc != null) {
            return ResponseEntity.ok(pc);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<ProductCategory> createProductCategory(@RequestBody ProductCategory productCategory) {
        ProductCategory saved = productCategoryService.saveProductCategory(productCategory);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductCategory> updateProductCategory(@PathVariable("id") int id, @RequestBody ProductCategory productCategory) {
        ProductCategory updated = productCategoryService.updateProductCategory(id, productCategory);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductCategory(@PathVariable("id") int id) {
        if (productCategoryService.deleteProductCategory(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
