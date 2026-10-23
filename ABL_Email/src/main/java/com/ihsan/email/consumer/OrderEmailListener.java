package com.ihsan.email.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.ihsan.email.config.RabbitMQConfig;
import com.ihsan.email.dto.OrderEmailEvent;
import com.ihsan.email.service.EmailService;

import tools.jackson.databind.ObjectMapper;

@Service
public class OrderEmailListener {

	private final ObjectMapper objectMapper;
	private final EmailService emailService;

	public OrderEmailListener(ObjectMapper objectMapper, EmailService emailService) {
		this.objectMapper = objectMapper;
		this.emailService = emailService;
	}

	@RabbitListener(queues = RabbitMQConfig.ORDER_EMAIL_QUEUE)
	public void sendOrderEmail(String payload) throws Exception {
		OrderEmailEvent event = objectMapper.readValue(payload, OrderEmailEvent.class);
		emailService.sendOrderConfirmation(event);
	}
}
