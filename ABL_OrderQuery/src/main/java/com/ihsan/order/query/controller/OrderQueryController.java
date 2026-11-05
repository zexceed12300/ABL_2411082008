package com.ihsan.order.query.controller;

import com.ihsan.order.query.dto.Customer;
import com.ihsan.order.query.dto.ResponseTemplate;
import com.ihsan.order.query.entity.Order;
import com.ihsan.order.query.middleware.RequireRole;
import com.ihsan.order.query.service.OrderQueryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoint sisi read CQRS. Seluruh operasi tulis (POST/PUT/DELETE) dilayani oleh
 * Order Service (Command/Write side).
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
    @RequireRole({"CUSTOMER", "ADMIN"})
    public ResponseEntity<List<Order>> getAllOrder(
            @RequestParam(value = "product-id", required = false) Integer productId,
            @RequestParam(value = "customer-id", required = false) Integer customerId,
            HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        String userEmail = (String) request.getAttribute("email");

        // Jika CUSTOMER biasa, batasi hanya order miliknya sendiri
        if ("CUSTOMER".equalsIgnoreCase(role) && userEmail != null) {
            Integer myCustomerId = orderQueryService.findCustomerIdByEmail(userEmail);
            if (myCustomerId == null) {
                return ResponseEntity.ok(List.of());
            }
            return ResponseEntity.ok(orderQueryService.getAllOrder(productId, myCustomerId));
        }

        // Jika ADMIN, bisa melihat seluruh order atau filter customerId manapun
        return ResponseEntity.ok(orderQueryService.getAllOrder(productId, customerId));
    }

    @GetMapping("/{id}")
    @RequireRole({"CUSTOMER", "ADMIN"})
    public ResponseEntity<?> getOrderById(@PathVariable("id") int id, HttpServletRequest request) {
        List<ResponseTemplate> result = orderQueryService.getOrderWithProductById(id);
        if (result == null || result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String role = (String) request.getAttribute("role");
        String userEmail = (String) request.getAttribute("email");

        // Jika CUSTOMER biasa, pastikan order ini milik dirinya
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
}
