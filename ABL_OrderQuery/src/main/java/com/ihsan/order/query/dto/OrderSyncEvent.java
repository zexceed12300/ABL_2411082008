package com.ihsan.order.query.dto;

import java.time.LocalDate;

/**
 * Salinan kontrak event dari Order Command Service. Field flat agar mapping
 * langsung tanpa konfigurasi tambahan.
 */
public class OrderSyncEvent {

    public static final String CREATED = "CREATED";
    public static final String UPDATED = "UPDATED";
    public static final String DELETED = "DELETED";

    private String eventType;
    private Integer orderId;
    private Integer productId;
    private Integer customerId;
    private LocalDate transactionDate;
    private Integer amount;
    private Double total;

    public OrderSyncEvent() {
    }

    public OrderSyncEvent(String eventType, Integer orderId, Integer productId, Integer customerId,
            LocalDate transactionDate, Integer amount, Double total) {
        this.eventType = eventType;
        this.orderId = orderId;
        this.productId = productId;
        this.customerId = customerId;
        this.transactionDate = transactionDate;
        this.amount = amount;
        this.total = total;
    }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }

    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }

    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
}
