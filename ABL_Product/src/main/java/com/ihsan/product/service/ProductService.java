package com.ihsan.product.service;

import com.ihsan.product.entity.Product;
import com.ihsan.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    // 1. Get all products
    public List<Product> getAllProduct() {
        return productRepository.findAll();
    }

    public List<Product> getAllProduct(Integer productCategoryId) {
        if (productCategoryId != null) {
            return productRepository.findByProductCategoryId(productCategoryId);
        }
        return productRepository.findAll();
    }

    // 2. Get product by ID
    public Product getProductById(int id) {
        return productRepository.findById(id).orElse(null);
    }

    // 3. Save new product
    public Product saveProduct(Product product) {
        product.setId(null); // Reset id so database generates a new ID and prevents 500 error if client sends id
        return productRepository.save(product);
    }

    // 4. Update product data
    public Product updateProduct(int id, Product product) {
        Product existing = productRepository.findById(id).orElse(null);
        if (existing != null) {
            existing.setName(product.getName());
            existing.setPrice(product.getPrice());
            existing.setDescription(product.getDescription());
            if (product.getProductCategoryId() != null) {
                existing.setProductCategoryId(product.getProductCategoryId());
            }
            return productRepository.save(existing);
        }
        return null;
    }

    // 5. Delete product by ID
    public boolean deleteProduct(int id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // 6. Get products by category ID
    public List<Product> getProductByCategoryId(int categoryId) {
        return productRepository.findByProductCategoryId(categoryId);
    }
}
