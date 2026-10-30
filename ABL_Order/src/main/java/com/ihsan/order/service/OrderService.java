package com.ihsan.order.service;

import com.ihsan.order.OrderConfig;
import com.ihsan.order.entity.Order;
import com.ihsan.order.repository.OrderRepository;
import com.ihsan.order.dto.Customer;
import com.ihsan.order.dto.OrderEmailEvent;
import com.ihsan.order.dto.OrderSyncEvent;
import com.ihsan.order.dto.Product;
import com.ihsan.order.dto.ResponseTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // 1. Mengambil semua data order
    public List<Order> getAllOrder() {
        return orderRepository.findAll();
    }

    public List<Order> getAllOrder(Integer productId, Integer customerId) {
        if (productId != null) {
            return orderRepository.findByProductId(productId);
        }
        if (customerId != null) {
            return orderRepository.findByCustomerId(customerId);
        }
        return orderRepository.findAll();
    }

    // 2. Mengambil order berdasarkan ID
    public Order getOrderById(int id) {
        return orderRepository.findById(id).orElse(null);
    }

    // 3. Menyimpan order baru
    public Order saveOrder(Order order) {
        order.setId(null); // Reset id agar database meng-generate ID baru secara otomatis dan mencegah 500 error jika client mengirim field id
        Product product = getProductForOrder(order);
        Customer customer = getCustomerForOrder(order);
        if (product == null || product.getPrice() <= 0) {
            throw new IllegalArgumentException("Product tidak ditemukan atau harga tidak valid");
        }
        if (customer == null || customer.getEmail() == null || customer.getEmail().isBlank()) {
            throw new IllegalArgumentException("Customer tidak ditemukan atau belum memiliki email");
        }
        order.setTotal(product.getPrice() * order.getAmount());
        Order saved = orderRepository.save(order);
        sendOrderEmailEvent(saved, product, customer);
        publishSyncEvent(OrderSyncEvent.of(OrderSyncEvent.CREATED, saved));
        return saved;
    }

    // 4. Memperbarui data order
    public Order updateOrder(int id, Order order) {
        Order orderAda = orderRepository.findById(id).orElse(null);
        if (orderAda != null) {
            orderAda.setProductId(order.getProductId());
            orderAda.setCustomerId(order.getCustomerId());
            orderAda.setTransactionDate(order.getTransactionDate());
            orderAda.setAmount(order.getAmount());
            orderAda.setTotal(order.getTotal());
            Order saved = orderRepository.save(orderAda);
            publishSyncEvent(OrderSyncEvent.of(OrderSyncEvent.UPDATED, saved));
            return saved;
        }
        return null;
    }

    // 5. Menghapus order berdasarkan ID
    public boolean deleteOrder(int id) {
        if (orderRepository.existsById(id)) {
            orderRepository.deleteById(id);
            publishSyncEvent(OrderSyncEvent.deleted(id));
            return true;
        }
        return false;
    }

    // 6. Mengambil order berdasarkan product ID
    public List<Order> getOrderByProductId(int productId) {
        return orderRepository.findByProductId(productId);
    }

    // 7. Mengambil order berdasarkan customer ID
    public List<Order> getOrderByCustomerId(int customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    // 8. Mengambil order lengkap dengan Product dan Customer
    public List<ResponseTemplate> getOrderWithProductById(int id) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            return null;
        }

        List<ServiceInstance> productInstances = discoveryClient.getInstances("product");
        List<ServiceInstance> customerInstances = discoveryClient.getInstances("pelanggan");
        if (productInstances.isEmpty() || customerInstances.isEmpty()) {
            return null;
        }

        String productUrl = productInstances.get(0).getUri().toString();
        String customerUrl = customerInstances.get(0).getUri().toString();
        Product product = restTemplate.getForObject(productUrl + "/api/product/" + order.getProductId(), Product.class);
        Customer customer = restTemplate.getForObject(customerUrl + "/api/customer/" + order.getCustomerId(), Customer.class);

        ResponseTemplate responseTemplate = new ResponseTemplate(order, product, customer);
        return List.of(responseTemplate);
    }

    private Product getProductForOrder(Order order) {
        List<ServiceInstance> instances = discoveryClient.getInstances("product");
        if (instances.isEmpty()) {
            return null;
        }
        return restTemplate.getForObject(instances.get(0).getUri() + "/api/product/" + order.getProductId(),
                Product.class);
    }

    private Customer getCustomerForOrder(Order order) {
        List<ServiceInstance> instances = discoveryClient.getInstances("pelanggan");
        if (instances.isEmpty()) {
            return null;
        }
        return restTemplate.getForObject(instances.get(0).getUri() + "/api/customer/" + order.getCustomerId(),
                Customer.class);
    }

    private void sendOrderEmailEvent(Order order, Product product, Customer customer) {
        OrderEmailEvent event = new OrderEmailEvent(Long.valueOf(order.getId()), customer.getName(),
                customer.getEmail(), product.getName(), order.getAmount(), BigDecimal.valueOf(product.getPrice()),
                BigDecimal.valueOf(order.getTotal()), order.getTransactionDate());
        try {
            rabbitTemplate.convertAndSend(OrderConfig.ORDER_EMAIL_QUEUE, objectMapper.writeValueAsString(event));
        } catch (RuntimeException exception) {
            logger.error("Order {} tersimpan, tetapi event email gagal dikirim ke RabbitMQ", order.getId(),
                    exception);
        }
    }

    private void publishSyncEvent(OrderSyncEvent event) {
        try {
            rabbitTemplate.convertAndSend(OrderConfig.ORDER_SYNC_QUEUE, objectMapper.writeValueAsString(event));
        } catch (RuntimeException exception) {
            logger.error("Order {} tersimpan, tetapi event sinkronisasi {} gagal dikirim ke RabbitMQ",
                    event.getOrderId(), event.getEventType(), exception);
        }
    }
}
