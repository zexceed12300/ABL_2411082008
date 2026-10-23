package com.ihsan.email.service;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.ihsan.email.dto.OrderEmailEvent;

@Service
public class EmailService {

	private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
	private final JavaMailSender mailSender;
	private final String fromAddress;

	public EmailService(JavaMailSender mailSender,
			@Value("${order.email.from:}") String fromAddress) {
		this.mailSender = mailSender;
		this.fromAddress = fromAddress;
	}

	public void sendOrderConfirmation(OrderEmailEvent event) {
		String recipient = event.getRecipientEmail();
		if (recipient == null || recipient.isBlank()) {
			throw new IllegalArgumentException("Event order tidak memiliki email penerima");
		}

		Long orderId = event.getOrderId();
		String customerName = event.getCustomerName();
		String productName = event.getProductName();
		int quantity = event.getQuantity() == null ? 0 : event.getQuantity();
		BigDecimal unitPrice = event.getUnitPrice();
		BigDecimal total = event.getTotal();
		String orderDate = event.getOrderDate() == null
				? "Tidak dicantumkan"
				: event.getOrderDate().toString();

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
