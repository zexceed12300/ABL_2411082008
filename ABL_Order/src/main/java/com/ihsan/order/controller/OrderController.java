package com.ihsan.order.controller;

import com.ihsan.order.dto.ResponseTemplate;
import com.ihsan.order.entity.Order;
import com.ihsan.order.middleware.RequireRole;
import com.ihsan.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    @RequireRole({"CUSTOMER", "ADMIN"})
    public ResponseEntity<List<Order>> getAllOrder(
            @RequestParam(value = "product-id", required = false) Integer productId,
            @RequestParam(value = "customer-id", required = false) Integer customerId) {
        return ResponseEntity.ok(orderService.getAllOrder(productId, customerId));
    }

    @GetMapping("/{id}")
    @RequireRole({"CUSTOMER", "ADMIN"})
    public List<ResponseTemplate> getOrderById(@PathVariable("id") int id) {
        return orderService.getOrderWithProductById(id);
    }

    @PostMapping
    @RequireRole({"CUSTOMER", "ADMIN"})
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        try {
            Order saved = orderService.saveOrder(order);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<Order> updateOrder(@PathVariable("id") int id, @RequestBody Order order) {
        Order updated = orderService.updateOrder(id, order);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<Void> deleteOrder(@PathVariable("id") int id) {
        boolean deleted = orderService.deleteOrder(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
