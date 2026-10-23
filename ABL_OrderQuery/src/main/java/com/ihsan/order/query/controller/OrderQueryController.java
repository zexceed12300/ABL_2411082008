package com.ihsan.order.query.controller;

import com.ihsan.order.query.dto.ResponseTemplate;
import com.ihsan.order.query.entity.Order;
import com.ihsan.order.query.service.OrderQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoint sisi read. Seluruh operasi tulis (POST/PUT/DELETE) dilayani oleh
 * Order Command Service.
 */
@RestController
@RequestMapping("/api/order")
public class OrderQueryController {

    private final OrderQueryService orderQueryService;

    @Autowired
    public OrderQueryController(OrderQueryService orderQueryService) {
        this.orderQueryService = orderQueryService;
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrder(
            @RequestParam(value = "product-id", required = false) Integer productId,
            @RequestParam(value = "customer-id", required = false) Integer customerId) {
        return ResponseEntity.ok(orderQueryService.getAllOrder(productId, customerId));
    }

    @GetMapping("/{id}")
    public List<ResponseTemplate> getOrderById(@PathVariable("id") int id) {
        return orderQueryService.getOrderWithProductById(id);
    }
}
