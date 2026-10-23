package com.example.emailservice;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import tools.jackson.databind.ObjectMapper;

@Service
public class OrderEmailListener {

	private static final Logger logger = LoggerFactory.getLogger(OrderEmailListener.class);
	private final ObjectMapper objectMapper;
	private final JavaMailSender mailSender;
	private final String fromAddress;

	public OrderEmailListener(ObjectMapper objectMapper, JavaMailSender mailSender,
			@Value("${order.email.from:}") String fromAddress) {
		this.objectMapper = objectMapper;
		this.mailSender = mailSender;
		this.fromAddress = fromAddress;
	}

	@RabbitListener(queues = RabbitMQConfig.ORDER_EMAIL_QUEUE)
	public void sendOrderEmail(String payload) throws Exception {
		var event = objectMapper.readTree(payload);
		String recipient = event.path("recipientEmail").stringValue();
		if (recipient.isBlank()) {
			throw new IllegalArgumentException("Event order tidak memiliki email penerima");
		}

		Long orderId = event.path("orderId").asLong();
		String customerName = event.path("customerName").stringValue();
		String productName = event.path("productName").stringValue();
		int quantity = event.path("quantity").asInt();
		BigDecimal unitPrice = event.path("unitPrice").decimalValue();
		BigDecimal total = event.path("total").decimalValue();
		String orderDate = event.path("orderDate").isNull()
				? "Tidak dicantumkan"
				: LocalDate.parse(event.path("orderDate").stringValue()).toString();

		SimpleMailMessage email = new SimpleMailMessage();
		if (!fromAddress.isBlank()) {
			email.setFrom(fromAddress);
		}
		email.setTo(recipient);
		email.setSubject("Konfirmasi Order #" + orderId);
		email.setText("Halo " + customerName + ",\n\n"
				+ "Order Anda berhasil dibuat.\n"
				+ "Nomor order: " + orderId + "\n"
				+ "Produk: " + productName + "\n"
				+ "Jumlah: " + quantity + "\n"
				+ "Harga satuan: " + unitPrice.toPlainString() + "\n"
				+ "Total: " + total.toPlainString() + "\n"
				+ "Tanggal: " + orderDate + "\n\n"
				+ "Terima kasih.");
		mailSender.send(email);
		logger.info("Konfirmasi order {} dikirim ke {}", orderId, recipient);
	}
}