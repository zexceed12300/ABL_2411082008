package com.ihsan.order.service;

import com.ihsan.order.entity.Order;
import com.ihsan.order.repository.OrderRepository;
import com.ihsan.order.dto.Customer;
import com.ihsan.order.dto.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private static final ServiceInstance PRODUCT_INSTANCE =
            new DefaultServiceInstance("product-1", "product", "localhost", 8080, false);
    private static final ServiceInstance CUSTOMER_INSTANCE =
            new DefaultServiceInstance("pelanggan-1", "pelanggan", "localhost", 8081, false);

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private DiscoveryClient discoveryClient;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private OrderService orderService;

    @Test
    void saveOrderRecomputesTotalAndPublishesEmailAndSyncEvents() throws Exception {
        var mapperField = OrderService.class.getDeclaredField("objectMapper");
        mapperField.setAccessible(true);
        mapperField.set(orderService, JsonMapper.builder().build());

        Product product = new Product(7, "Kopi Susu", 25000.0, "Enak");
        Customer customer = new Customer(3, "Budi", "budi@example.com", "Jl. Test", "Laki-laki");

        when(discoveryClient.getInstances("product")).thenReturn(List.of(PRODUCT_INSTANCE));
        when(discoveryClient.getInstances("pelanggan")).thenReturn(List.of(CUSTOMER_INSTANCE));
        when(restTemplate.getForObject(PRODUCT_INSTANCE.getUri() + "/api/product/7", Product.class))
                .thenReturn(product);
        when(restTemplate.getForObject(CUSTOMER_INSTANCE.getUri() + "/api/customer/3", Customer.class))
                .thenReturn(customer);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order toSave = invocation.getArgument(0);
            toSave.setId(42);
            return toSave;
        });

        Order request = new Order(999, 7, 3, LocalDate.of(2026, 10, 16), 2, 1.0);

        Order saved = orderService.saveOrder(request);

        assertThat(saved.getId()).isEqualTo(42);
        assertThat(saved.getTotal()).isEqualTo(25000.0 * 2);

        // Verify email event published
        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        verify(rabbitTemplate).convertAndSend(eq("order.email.queue"), emailCaptor.capture());
        assertThat(emailCaptor.getValue()).contains("\"recipientEmail\":\"budi@example.com\"");

        // Verify sync event published to order.sync.queue
        ArgumentCaptor<String> syncCaptor = ArgumentCaptor.forClass(String.class);
        verify(rabbitTemplate).convertAndSend(eq("order.sync.queue"), syncCaptor.capture());
        assertThat(syncCaptor.getValue()).contains("\"eventType\":\"CREATED\"");
        assertThat(syncCaptor.getValue()).contains("\"orderId\":42");
    }

    @Test
    void saveOrderWithoutCustomerEmailThrowsBeforePublish() {
        Product product = new Product(7, "Kopi Susu", 25000.0, "Enak");
        Customer customer = new Customer(3, "Budi", null, "Jl. Test", "Laki-laki");

        when(discoveryClient.getInstances("product")).thenReturn(List.of(PRODUCT_INSTANCE));
        when(discoveryClient.getInstances("pelanggan")).thenReturn(List.of(CUSTOMER_INSTANCE));
        when(restTemplate.getForObject(PRODUCT_INSTANCE.getUri() + "/api/product/7", Product.class))
                .thenReturn(product);
        when(restTemplate.getForObject(CUSTOMER_INSTANCE.getUri() + "/api/customer/3", Customer.class))
                .thenReturn(customer);

        Order request = new Order(null, 7, 3, LocalDate.of(2026, 10, 16), 2, 0.0);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> orderService.saveOrder(request))
                .isInstanceOf(IllegalArgumentException.class);
        org.mockito.Mockito.verifyNoInteractions(rabbitTemplate);
        org.mockito.Mockito.verifyNoInteractions(orderRepository);
    }
}
