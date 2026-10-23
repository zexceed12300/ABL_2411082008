package com.ihsan.order.command.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class OrderCommandConfig {

    public static final String ORDER_EMAIL_QUEUE = "order.email.queue";
    public static final String ORDER_SYNC_QUEUE = "order.sync.queue";

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public Queue orderEmailQueue() {
        return new Queue(ORDER_EMAIL_QUEUE, true);
    }

    @Bean
    public Queue orderSyncQueue() {
        return new Queue(ORDER_SYNC_QUEUE, true);
    }
}
