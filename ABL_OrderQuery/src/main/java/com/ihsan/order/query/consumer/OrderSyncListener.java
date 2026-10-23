package com.ihsan.order.query.consumer;

import com.ihsan.order.query.config.OrderQueryConfig;
import com.ihsan.order.query.dto.OrderSyncEvent;
import com.ihsan.order.query.service.OrderQueryService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * Menerima event sinkronisasi dari Order Command Service dan menerapkannya
 * ke read model.
 */
@Component
public class OrderSyncListener {

    private final ObjectMapper objectMapper;
    private final OrderQueryService orderQueryService;

    public OrderSyncListener(ObjectMapper objectMapper, OrderQueryService orderQueryService) {
        this.objectMapper = objectMapper;
        this.orderQueryService = orderQueryService;
    }

    @RabbitListener(queues = OrderQueryConfig.ORDER_SYNC_QUEUE)
    public void onOrderChanged(String payload) throws Exception {
        OrderSyncEvent event = objectMapper.readValue(payload, OrderSyncEvent.class);
        orderQueryService.apply(event);
    }
}
