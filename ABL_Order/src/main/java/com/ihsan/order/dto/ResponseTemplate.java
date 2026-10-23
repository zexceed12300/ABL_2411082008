package com.ihsan.order.dto;

import com.ihsan.order.entity.Order;

public class ResponseTemplate {

    private Order order;
    private Product product;
    private Customer customer;

    public ResponseTemplate() {
    }

    public ResponseTemplate(Order order, Product product, Customer customer) {
        this.order = order;
        this.product = product;
        this.customer = customer;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}