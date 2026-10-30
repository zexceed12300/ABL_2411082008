package com.ihsan.order.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class OrderEmailEvent {

    private Long orderId;
    private String customerName;
    private String recipientEmail;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal total;
    private LocalDate orderDate;

    public OrderEmailEvent() {
    }

    public OrderEmailEvent(Long orderId, String customerName, String recipientEmail, String productName,
            Integer quantity, BigDecimal unitPrice, BigDecimal total, LocalDate orderDate) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.recipientEmail = recipientEmail;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.total = total;
        this.orderDate = orderDate;
    }

    public Long getOrderId() { return orderId; }
    public String getCustomerName() { return customerName; }
    public String getRecipientEmail() { return recipientEmail; }
    public String getProductName() { return productName; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getTotal() { return total; }
    public LocalDate getOrderDate() { return orderDate; }
}
