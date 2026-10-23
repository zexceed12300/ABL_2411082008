package com.ihsan.order.command.service;

import com.ihsan.order.command.config.OrderCommandConfig;
import com.ihsan.order.command.dto.Customer;
import com.ihsan.order.command.dto.OrderEmailEvent;
import com.ihsan.order.command.dto.OrderSyncEvent;
import com.ihsan.order.command.dto.Product;
import com.ihsan.order.command.entity.Order;
import com.ihsan.order.command.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

/**
 * Sisi write pada CQRS. Hanya menangani insert, update, dan delete.
 * Setiap perubahan dipublikasikan ke RabbitMQ agar Query Service tetap sinkron.
 */
@Service
public class OrderCommandService {

    private static final Logger logger = LoggerFactory.getLogger(OrderCommandService.class);

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

    // 1. Menyimpan order baru
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

    // 2. Memperbarui data order
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

    // 3. Menghapus order berdasarkan ID
    public boolean deleteOrder(int id) {
        if (orderRepository.existsById(id)) {
            orderRepository.deleteById(id);
            publishSyncEvent(OrderSyncEvent.deleted(id));
            return true;
        }
        return false;
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
            rabbitTemplate.convertAndSend(OrderCommandConfig.ORDER_EMAIL_QUEUE, objectMapper.writeValueAsString(event));
        } catch (RuntimeException exception) {
            logger.error("Order {} tersimpan, tetapi event email gagal dikirim ke RabbitMQ", order.getId(),
                    exception);
        }
    }

    /**
     * Mengirim event sinkronisasi ke Query Service. Kegagalan pengiriman tidak
     * membatalkan perubahan data, tetapi dicatat di log.
     */
    private void publishSyncEvent(OrderSyncEvent event) {
        try {
            rabbitTemplate.convertAndSend(OrderCommandConfig.ORDER_SYNC_QUEUE, objectMapper.writeValueAsString(event));
        } catch (RuntimeException exception) {
            logger.error("Order {} tersimpan, tetapi event sinkronisasi {} gagal dikirim ke RabbitMQ",
                    event.getOrderId(), event.getEventType(), exception);
        }
    }
}
