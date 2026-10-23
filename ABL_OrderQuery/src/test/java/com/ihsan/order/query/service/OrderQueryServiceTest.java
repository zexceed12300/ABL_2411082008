package com.ihsan.order.query.service;

import com.ihsan.order.query.dto.OrderSyncEvent;
import com.ihsan.order.query.entity.Order;
import com.ihsan.order.query.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderQueryServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderQueryService orderQueryService;

    @Test
    void applyCreatedSavesOrderWithIdFromEvent() {
        OrderSyncEvent event = new OrderSyncEvent(OrderSyncEvent.CREATED, 42, 7, 3,
                LocalDate.of(2026, 10, 16), 2, 50000.0);

        orderQueryService.apply(event);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        Order saved = captor.getValue();

        assertThat(saved.getId()).isEqualTo(42);
        assertThat(saved.getProductId()).isEqualTo(7);
        assertThat(saved.getCustomerId()).isEqualTo(3);
        assertThat(saved.getTransactionDate()).isEqualTo(LocalDate.of(2026, 10, 16));
        assertThat(saved.getAmount()).isEqualTo(2);
        assertThat(saved.getTotal()).isEqualTo(50000.0);
    }

    @Test
    void applyUpdatedOverwritesExistingOrder() {
        OrderSyncEvent event = new OrderSyncEvent(OrderSyncEvent.UPDATED, 42, 9, 5,
                LocalDate.of(2026, 11, 1), 4, 100000.0);

        orderQueryService.apply(event);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        Order saved = captor.getValue();

        assertThat(saved.getId()).isEqualTo(42);
        assertThat(saved.getProductId()).isEqualTo(9);
        assertThat(saved.getCustomerId()).isEqualTo(5);
        assertThat(saved.getAmount()).isEqualTo(4);
        assertThat(saved.getTotal()).isEqualTo(100000.0);
    }

    @Test
    void applyCreatedTwiceStillCallsSaveWithSameId() {
        OrderSyncEvent event = new OrderSyncEvent(OrderSyncEvent.CREATED, 42, 7, 3,
                LocalDate.of(2026, 10, 16), 2, 50000.0);

        orderQueryService.apply(event);
        orderQueryService.apply(event);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, times(2)).save(captor.capture());
        // JPA merge pada id yang sama tidak membuat baris ganda.
        assertThat(captor.getAllValues()).allSatisfy(order -> assertThat(order.getId()).isEqualTo(42));
    }

    @Test
    void applyDeletedRemovesOrder() {
        OrderSyncEvent event = new OrderSyncEvent(OrderSyncEvent.DELETED, 42, null, null, null, null, null);

        orderQueryService.apply(event);

        verify(orderRepository).deleteById(42);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void applyWithUnknownEventTypeThrows() {
        OrderSyncEvent event = new OrderSyncEvent("SOMETHING", 42, null, null, null, null, null);

        assertThatThrownBy(() -> orderQueryService.apply(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("eventType tidak dikenal");
    }

    @Test
    void applyWithoutOrderIdThrows() {
        OrderSyncEvent event = new OrderSyncEvent(OrderSyncEvent.CREATED, null, 7, 3,
                LocalDate.of(2026, 10, 16), 2, 50000.0);

        assertThatThrownBy(() -> orderQueryService.apply(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("orderId");
    }

    @Test
    void getAllOrderFiltersByProductId() {
        Order order = new Order(42, 7, 3, LocalDate.of(2026, 10, 16), 2, 50000.0);
        when(orderRepository.findByProductId(7)).thenReturn(List.of(order));

        List<Order> result = orderQueryService.getAllOrder(7, null);

        assertThat(result).containsExactly(order);
        verify(orderRepository, never()).findAll();
    }

    @Test
    void getAllOrderFiltersByCustomerId() {
        Order order = new Order(42, 7, 3, LocalDate.of(2026, 10, 16), 2, 50000.0);
        when(orderRepository.findByCustomerId(3)).thenReturn(List.of(order));

        List<Order> result = orderQueryService.getAllOrder(null, 3);

        assertThat(result).containsExactly(order);
        verify(orderRepository, never()).findAll();
    }

    @Test
    void getAllOrderWithoutFilterReturnsAll() {
        Order order = new Order(42, 7, 3, LocalDate.of(2026, 10, 16), 2, 50000.0);
        when(orderRepository.findAll()).thenReturn(List.of(order));

        List<Order> result = orderQueryService.getAllOrder(null, null);

        assertThat(result).containsExactly(order);
    }

    @Test
    void getOrderByIdReturnsEmptyWhenMissing() {
        when(orderRepository.findById(99)).thenReturn(Optional.empty());

        assertThat(orderQueryService.getOrderById(99)).isNull();
    }
}
