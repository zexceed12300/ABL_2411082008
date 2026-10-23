package com.ihsan.order.command.service;

import com.ihsan.order.command.dto.Customer;
import com.ihsan.order.command.dto.Product;
import com.ihsan.order.command.entity.Order;
import com.ihsan.order.command.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderCommandServiceTest {

    private static final String EMAIL_QUEUE = "order.email.queue";
    private static final String SYNC_QUEUE = "order.sync.queue";

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
    private OrderCommandService orderCommandService;

    private void injectRealObjectMapper() throws Exception {
        // Real Jackson mapper (same tools.jackson the service autowires), injected via reflection
        // because @InjectMocks cannot build it.
        var mapperField = OrderCommandService.class.getDeclaredField("objectMapper");
        mapperField.setAccessible(true);
        mapperField.set(orderCommandService, JsonMapper.builder().build());
    }

    private void stubProductAndCustomer(String customerEmail) {
        Product product = new Product(7, "Kopi Susu", 25000.0, "Enak");
        Customer customer = new Customer(3, "Budi", customerEmail, "Jl. Test", "Laki-laki");

        when(discoveryClient.getInstances("product")).thenReturn(List.of(PRODUCT_INSTANCE));
        when(discoveryClient.getInstances("pelanggan")).thenReturn(List.of(CUSTOMER_INSTANCE));
        when(restTemplate.getForObject(PRODUCT_INSTANCE.getUri() + "/api/product/7", Product.class))
                .thenReturn(product);
        when(restTemplate.getForObject(CUSTOMER_INSTANCE.getUri() + "/api/customer/3", Customer.class))
                .thenReturn(customer);
    }

    @Test
    void saveOrderRecomputesTotalAndPublishesEmailAndSyncEvents() throws Exception {
        injectRealObjectMapper();
        stubProductAndCustomer("budi@example.com");
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order toSave = invocation.getArgument(0);
            toSave.setId(42);
            return toSave;
        });

        // Client sends a bogus total (1) and a stray id; both must be overridden.
        Order request = new Order(999, 7, 3, LocalDate.of(2026, 10, 16), 2, 1.0);

        Order saved = orderCommandService.saveOrder(request);

        assertThat(saved.getId()).isEqualTo(42);
        assertThat(saved.getTotal()).isEqualTo(25000.0 * 2);

        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        verify(rabbitTemplate).convertAndSend(eq(EMAIL_QUEUE), emailCaptor.capture());
        String emailJson = emailCaptor.getValue();

        assertThat(emailJson).contains("\"recipientEmail\":\"budi@example.com\"");
        assertThat(emailJson).contains("\"orderId\":42");
        assertThat(emailJson).contains("\"customerName\":\"Budi\"");
        assertThat(emailJson).contains("\"productName\":\"Kopi Susu\"");
        assertThat(emailJson).contains("\"quantity\":2");
        assertThat(emailJson).contains("\"unitPrice\":25000.0");
        assertThat(emailJson).contains("\"total\":50000.0");
        assertThat(emailJson).contains("\"orderDate\":\"2026-10-16\"");

        ArgumentCaptor<String> syncCaptor = ArgumentCaptor.forClass(String.class);
        verify(rabbitTemplate).convertAndSend(eq(SYNC_QUEUE), syncCaptor.capture());
        String syncJson = syncCaptor.getValue();

        assertThat(syncJson).contains("\"eventType\":\"CREATED\"");
        assertThat(syncJson).contains("\"orderId\":42");
        assertThat(syncJson).contains("\"productId\":7");
        assertThat(syncJson).contains("\"customerId\":3");
        assertThat(syncJson).contains("\"amount\":2");
        assertThat(syncJson).contains("\"total\":50000.0");
    }

    @Test
    void saveOrderWithoutCustomerEmailThrowsBeforePublish() {
        stubProductAndCustomer(null);

        Order request = new Order(null, 7, 3, LocalDate.of(2026, 10, 16), 2, 0.0);

        assertThatThrownBy(() -> orderCommandService.saveOrder(request))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(rabbitTemplate);
        verifyNoInteractions(orderRepository);
    }

    @Test
    void updateOrderPublishesUpdatedSyncEvent() throws Exception {
        injectRealObjectMapper();
        Order existing = new Order(42, 7, 3, LocalDate.of(2026, 10, 16), 1, 25000.0);
        when(orderRepository.findById(42)).thenReturn(Optional.of(existing));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order request = new Order(null, 8, 4, LocalDate.of(2026, 10, 20), 3, 75000.0);

        Order updated = orderCommandService.updateOrder(42, request);

        assertThat(updated).isNotNull();
        assertThat(updated.getId()).isEqualTo(42);
        assertThat(updated.getProductId()).isEqualTo(8);
        assertThat(updated.getCustomerId()).isEqualTo(4);
        assertThat(updated.getAmount()).isEqualTo(3);

        ArgumentCaptor<String> syncCaptor = ArgumentCaptor.forClass(String.class);
        verify(rabbitTemplate).convertAndSend(eq(SYNC_QUEUE), syncCaptor.capture());
        String syncJson = syncCaptor.getValue();

        assertThat(syncJson).contains("\"eventType\":\"UPDATED\"");
        assertThat(syncJson).contains("\"orderId\":42");
        assertThat(syncJson).contains("\"productId\":8");
        assertThat(syncJson).contains("\"customerId\":4");
        assertThat(syncJson).contains("\"amount\":3");
        assertThat(syncJson).contains("\"total\":75000.0");
    }

    @Test
    void updateOrderNotFoundPublishesNothing() {
        when(orderRepository.findById(99)).thenReturn(Optional.empty());

        Order updated = orderCommandService.updateOrder(99, new Order(null, 8, 4, LocalDate.of(2026, 10, 20), 3, 0.0));

        assertThat(updated).isNull();
        verify(rabbitTemplate, never()).convertAndSend(eq(SYNC_QUEUE), any(String.class));
    }

    @Test
    void deleteOrderPublishesDeletedSyncEvent() throws Exception {
        injectRealObjectMapper();
        when(orderRepository.existsById(42)).thenReturn(true);

        boolean deleted = orderCommandService.deleteOrder(42);

        assertThat(deleted).isTrue();
        verify(orderRepository).deleteById(42);

        ArgumentCaptor<String> syncCaptor = ArgumentCaptor.forClass(String.class);
        verify(rabbitTemplate).convertAndSend(eq(SYNC_QUEUE), syncCaptor.capture());
        String syncJson = syncCaptor.getValue();

        assertThat(syncJson).contains("\"eventType\":\"DELETED\"");
        assertThat(syncJson).contains("\"orderId\":42");
    }

    @Test
    void deleteOrderNotFoundPublishesNothing() {
        when(orderRepository.existsById(99)).thenReturn(false);

        boolean deleted = orderCommandService.deleteOrder(99);

        assertThat(deleted).isFalse();
        verify(orderRepository, never()).deleteById(99);
        verify(rabbitTemplate, never()).convertAndSend(eq(SYNC_QUEUE), any(String.class));
    }
}
