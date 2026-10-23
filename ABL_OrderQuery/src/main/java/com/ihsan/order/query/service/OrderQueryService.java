package com.ihsan.order.query.service;

import com.ihsan.order.query.dto.Customer;
import com.ihsan.order.query.dto.OrderSyncEvent;
import com.ihsan.order.query.dto.Product;
import com.ihsan.order.query.dto.ResponseTemplate;
import com.ihsan.order.query.entity.Order;
import com.ihsan.order.query.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Sisi read pada CQRS. Tidak pernah menulis data atas inisiatif sendiri:
 * satu-satunya jalur penulisan adalah {@link #apply(OrderSyncEvent)} yang
 * dipanggil listener RabbitMQ.
 */
@Service
public class OrderQueryService {

    private static final Logger logger = LoggerFactory.getLogger(OrderQueryService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Menerapkan event sinkronisasi ke read model. Operasinya idempoten:
     * CREATED dan UPDATED sama-sama memakai save() (JPA merge), sehingga event
     * yang terkirim dua kali tidak menghasilkan baris ganda.
     */
    public void apply(OrderSyncEvent event) {
        if (event.getEventType() == null) {
            throw new IllegalArgumentException("Event sinkronisasi tidak memiliki eventType");
        }
        switch (event.getEventType()) {
            case OrderSyncEvent.CREATED, OrderSyncEvent.UPDATED -> {
                if (event.getOrderId() == null) {
                    throw new IllegalArgumentException("Event " + event.getEventType() + " tidak memiliki orderId");
                }
                Order order = new Order(event.getOrderId(), event.getProductId(), event.getCustomerId(),
                        event.getTransactionDate(), event.getAmount(),
                        event.getTotal() == null ? 0.0 : event.getTotal());
                orderRepository.save(order);
                logger.info("Read model disinkronkan: {} order {}", event.getEventType(), event.getOrderId());
            }
            case OrderSyncEvent.DELETED -> {
                if (event.getOrderId() == null) {
                    throw new IllegalArgumentException("Event DELETED tidak memiliki orderId");
                }
                orderRepository.deleteById(event.getOrderId());
                logger.info("Read model disinkronkan: DELETED order {}", event.getOrderId());
            }
            default -> throw new IllegalArgumentException("eventType tidak dikenal: " + event.getEventType());
        }
    }

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

    // 3. Mengambil order berdasarkan product ID
    public List<Order> getOrderByProductId(int productId) {
        return orderRepository.findByProductId(productId);
    }

    // 4. Mengambil order berdasarkan customer ID
    public List<Order> getOrderByCustomerId(int customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    // 5. Mengambil order beserta product dan customer (dengan discovery client)
    public List<ResponseTemplate> getOrderWithProductById(int id) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            return null;
        }

        String productUrl = discoveryClient.getInstances("product").get(0).getUri().toString();
        String customerUrl = discoveryClient.getInstances("pelanggan").get(0).getUri().toString();
        Product product = restTemplate.getForObject(productUrl + "/api/product/" + order.getProductId(), Product.class);
        Customer customer = restTemplate.getForObject(customerUrl + "/api/customer/" + order.getCustomerId(), Customer.class);

        ResponseTemplate responseTemplate = new ResponseTemplate(order, product, customer);
        return List.of(responseTemplate);
    }
}
