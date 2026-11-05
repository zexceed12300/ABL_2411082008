package com.ihsan.customer.controller;

import com.ihsan.customer.entity.Customer;
import com.ihsan.customer.middleware.RequireRole;
import com.ihsan.customer.service.CustomerService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    private final CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    @RequireRole("ADMIN")
    public ResponseEntity<List<Customer>> getAllCustomer() {
        return ResponseEntity.ok(customerService.getAllCustomer());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<?> getCustomerByEmail(@PathVariable("email") String email, HttpServletRequest request) {
        Customer customer = customerService.findByEmail(email).orElse(null);
        if (customer == null) {
            return ResponseEntity.notFound().build();
        }

        String role = (String) request.getAttribute("role");
        String userEmail = (String) request.getAttribute("email");

        // Jika CUSTOMER, verifikasi hanya boleh lihat profil email sendiri
        if ("CUSTOMER".equalsIgnoreCase(role) && userEmail != null) {
            boolean isOwner = customer.getEmail() != null && customer.getEmail().equalsIgnoreCase(userEmail);
            if (!isOwner) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("{\"error\": \"Forbidden\", \"message\": \"Access denied: you can only view your own profile\"}");
            }
        }

        return ResponseEntity.ok(customer);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCustomerById(@PathVariable("id") int id, HttpServletRequest request) {
        Customer customer = customerService.getCustomerById(id);
        if (customer == null) {
            return ResponseEntity.notFound().build();
        }

        String role = (String) request.getAttribute("role");
        String userEmail = (String) request.getAttribute("email");

        // Jika CUSTOMER, verifikasi kepemilikan profile berdasarkan email
        if ("CUSTOMER".equalsIgnoreCase(role) && userEmail != null) {
            boolean isOwner = customer.getEmail() != null && customer.getEmail().equalsIgnoreCase(userEmail);
            if (!isOwner) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("{\"error\": \"Forbidden\", \"message\": \"Access denied: you can only view your own profile\"}");
            }
        }

        return ResponseEntity.ok(customer);
    }

    @PostMapping
    @RequireRole({"CUSTOMER", "ADMIN"})
    public ResponseEntity<?> createCustomer(@RequestBody Customer customer, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        String userEmail = (String) request.getAttribute("email");

        if ("CUSTOMER".equalsIgnoreCase(role) && userEmail != null) {
            // Otomatis ikat email customer dengan email akun login
            customer.setEmail(userEmail);

            // Cegah duplikasi profil customer untuk akun yang sama
            if (customerService.existsByEmail(userEmail)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("{\"error\": \"Conflict\", \"message\": \"Customer profile for this account already exists. Use PUT to update your profile.\"}");
            }
        } else if ("ADMIN".equalsIgnoreCase(role)) {
            // Validasi email unik untuk admin
            if (customer.getEmail() != null && customerService.existsByEmail(customer.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("{\"error\": \"Conflict\", \"message\": \"Customer with email " + customer.getEmail() + " already exists.\"}");
            }
        }

        Customer saved = customerService.saveCustomer(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @RequireRole({"CUSTOMER", "ADMIN"})
    public ResponseEntity<?> updateCustomer(@PathVariable("id") int id, @RequestBody Customer customer, HttpServletRequest request) {
        Customer existing = customerService.getCustomerById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }

        String role = (String) request.getAttribute("role");
        String userEmail = (String) request.getAttribute("email");

        if ("CUSTOMER".equalsIgnoreCase(role) && userEmail != null) {
            boolean isOwner = existing.getEmail() != null && existing.getEmail().equalsIgnoreCase(userEmail);
            if (!isOwner) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("{\"error\": \"Forbidden\", \"message\": \"Access denied: you can only update your own profile\"}");
            }
            // Pastikan email tidak diubah ke email lain
            customer.setEmail(userEmail);
        }

        Customer updated = customerService.updateCustomer(id, customer);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<Void> deleteCustomer(@PathVariable("id") int id) {
        if (customerService.deleteCustomer(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/multiply")
    public String multiply(@RequestParam("a") int a, @RequestParam("b") double b) {

        // convert a to integer, b to double, and call the multiply method in customerService
        // int x = Integer.parseInt(String.valueOf(a));
        // double y = Double.parseDouble(String.valueOf(b));
        
        double result = customerService.multiply(a, b);

        return "Hasil perkalian dari " + a + " dan " + b + " adalah: " + result;
        
    }
}
