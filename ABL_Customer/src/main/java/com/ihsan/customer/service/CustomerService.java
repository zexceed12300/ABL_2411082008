package com.ihsan.customer.service;

import com.ihsan.customer.entity.Customer;
import com.ihsan.customer.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<Customer> getAllCustomer() {
        return customerRepository.findAll();
    }

    public List<Customer> findByEmailOrName(String query) {
        if (query == null || query.isBlank()) {
            return customerRepository.findAll();
        }
        return customerRepository.findByEmailIgnoreCaseOrNameIgnoreCase(query.trim(), query.trim());
    }

    public Optional<Customer> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        return customerRepository.findByEmailIgnoreCase(email.trim());
    }

    public boolean existsByEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return customerRepository.existsByEmailIgnoreCase(email.trim());
    }

    public Customer getCustomerById(int id) {
        return customerRepository.findById(id).orElse(null);
    }

    public Customer saveCustomer(Customer customer) {
        customer.setId(null); // Reset id agar auto-increment dan bebas error 500
        return customerRepository.save(customer);
    }

    public Customer updateCustomer(int id, Customer customer) {
        Customer existing = customerRepository.findById(id).orElse(null);
        if (existing != null) {
            existing.setName(customer.getName());
            existing.setEmail(customer.getEmail());
            existing.setAddress(customer.getAddress());
            existing.setGender(customer.getGender());
            return customerRepository.save(existing);
        }
        return null;
    }

    public boolean deleteCustomer(int id) {
        if (customerRepository.existsById(id)) {
            customerRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // buatkan function untuk mengalikan bilangan bulat dan bilangan berkoma, 2 parameter fungsi
    public double multiply(int a, double b) {
        return a * b;
    }
}
