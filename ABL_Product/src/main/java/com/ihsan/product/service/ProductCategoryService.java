package com.ihsan.product.service;

import com.ihsan.product.entity.ProductCategory;
import com.ihsan.product.repository.ProductCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductCategoryService {

    private final ProductCategoryRepository productCategoryRepository;

    @Autowired
    public ProductCategoryService(ProductCategoryRepository productCategoryRepository) {
        this.productCategoryRepository = productCategoryRepository;
    }

    public List<ProductCategory> getAllProductCategory() {
        return productCategoryRepository.findAll();
    }

    public ProductCategory getProductCategoryById(int id) {
        return productCategoryRepository.findById(id).orElse(null);
    }

    public ProductCategory saveProductCategory(ProductCategory productCategory) {
        productCategory.setId(null); // Reset id for auto-increment to prevent 500 error
        return productCategoryRepository.save(productCategory);
    }

    public ProductCategory updateProductCategory(int id, ProductCategory productCategory) {
        ProductCategory existing = productCategoryRepository.findById(id).orElse(null);
        if (existing != null) {
            existing.setName(productCategory.getName());
            existing.setDescription(productCategory.getDescription());
            return productCategoryRepository.save(existing);
        }
        return null;
    }

    public boolean deleteProductCategory(int id) {
        if (productCategoryRepository.existsById(id)) {
            productCategoryRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
