package com.ihsan.order.controller;

import com.ihsan.order.dto.Customer;
import com.ihsan.order.dto.ResponseTemplate;
import com.ihsan.order.entity.Order;
import com.ihsan.order.middleware.RequireRole;
import com.ihsan.order.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
            @RequestParam(value = "customer-id", required = false) Integer customerId,
            HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        String userEmail = (String) request.getAttribute("email");

        if ("CUSTOMER".equalsIgnoreCase(role) && userEmail != null) {
            Integer myCustomerId = orderService.findCustomerIdByEmail(userEmail);
            if (myCustomerId == null) {
                return ResponseEntity.ok(List.of());
            }
            return ResponseEntity.ok(orderService.getAllOrder(productId, myCustomerId));
        }

        return ResponseEntity.ok(orderService.getAllOrder(productId, customerId));
    }

    @GetMapping("/{id}")
    @RequireRole({"CUSTOMER", "ADMIN"})
    public ResponseEntity<?> getOrderById(@PathVariable("id") int id, HttpServletRequest request) {
        List<ResponseTemplate> result = orderService.getOrderWithProductById(id);
        if (result == null || result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String role = (String) request.getAttribute("role");
        String userEmail = (String) request.getAttribute("email");

        if ("CUSTOMER".equalsIgnoreCase(role) && userEmail != null) {
            Customer customer = result.get(0).getCustomer();
            boolean isOwner = customer != null && customer.getEmail() != null && customer.getEmail().equalsIgnoreCase(userEmail);
            if (!isOwner) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("{\"error\": \"Forbidden\", \"message\": \"Access denied: you cannot view another customer's order\"}");
            }
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping
    @RequireRole({"CUSTOMER", "ADMIN"})
    public ResponseEntity<?> createOrder(@RequestBody Order order, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        String userEmail = (String) request.getAttribute("email");

        if ("CUSTOMER".equalsIgnoreCase(role) && userEmail != null) {
            Integer myCustomerId = orderService.findCustomerIdByEmail(userEmail);
            if (myCustomerId != null) {
                if (order.getCustomerId() == null) {
                    order.setCustomerId(myCustomerId);
                } else if (!order.getCustomerId().equals(myCustomerId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("{\"error\": \"Forbidden\", \"message\": \"Access denied: you cannot create orders for another customer\"}");
                }
            }
        }

        try {
            Order saved = orderService.saveOrder(order);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("{\"error\": \"Bad Request\", \"message\": \"" + e.getMessage() + "\"}");
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
