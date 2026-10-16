package com.ihsan.product.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private double price;
    private String description;

    @Column(name = "product_category_id")
    @JsonProperty("product_category_id")
    private Integer productCategoryId;

    public Product() {
    }

    public Product(Integer id, String name, double price, String description) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
    }

    public Product(Integer id, String name, double price, String description, Integer productCategoryId) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.productCategoryId = productCategoryId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("product_category_id")
    public Integer getProductCategoryId() {
        return productCategoryId;
    }

    @JsonProperty("product_category_id")
    public void setProductCategoryId(Integer productCategoryId) {
        this.productCategoryId = productCategoryId;
    }
}
