package com.ihsan.email.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

	public static final String ORDER_EMAIL_QUEUE = "order.email.queue";

	@Bean
	public Queue orderEmailQueue() {
		return new Queue(ORDER_EMAIL_QUEUE, true);
	}
}