package com.ihsan.order.command.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.http.ResponseEntity;
import com.ihsan.order.command.entity.Order;
import com.ihsan.order.command.service.OrderCommandService;

/**
 * Endpoint sisi write. Seluruh operasi baca (GET) dilayani oleh OrderQuery Service.
 */
@RestController
@RequestMapping("/api/order")
public class OrderCommandController {

    private final OrderCommandService orderCommandService;

    @Autowired
    public OrderCommandController(OrderCommandService orderCommandService) {
        this.orderCommandService = orderCommandService;
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        try {
            Order saved = orderCommandService.saveOrder(order);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(@PathVariable("id") int id, @RequestBody Order order) {
        Order updated = orderCommandService.updateOrder(id, order);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable("id") int id) {
        boolean deleted = orderCommandService.deleteOrder(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
