package com.ihsan.order.query.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * Read model untuk CQRS. ID TIDAK digenerate di sini: nilainya selalu berasal
 * dari event sinkronisasi yang dikirim Order Command Service.
 */
@Entity
@Table(name = "orders")
public class Order {

    @Id
    private Integer id;

    @Column(name = "product_id")
    @JsonProperty("product_id")
    private Integer productId;

    @Column(name = "customer_id")
    @JsonProperty("customer_id")
    private Integer customerId;

    @Column(name = "transaction_date")
    @JsonProperty("transaction_date")
    private LocalDate transactionDate;

    private Integer amount;
    private Double total;

    public Order() {
    }

    public Order(Integer id, Integer productId, Integer customerId, LocalDate transactionDate, Integer amount, Double total) {
        this.id = id;
        this.productId = productId;
        this.customerId = customerId;
        this.transactionDate = transactionDate;
        this.amount = amount;
        this.total = total;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @JsonProperty("product_id")
    public Integer getProductId() {
        return productId;
    }

    @JsonProperty("product_id")
    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    @JsonProperty("customer_id")
    public Integer getCustomerId() {
        return customerId;
    }

    @JsonProperty("customer_id")
    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    @JsonProperty("transaction_date")
    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    @JsonProperty("transaction_date")
    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }
}
