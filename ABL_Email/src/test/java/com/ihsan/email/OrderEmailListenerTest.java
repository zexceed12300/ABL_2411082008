package com.example.emailservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class OrderEmailListenerTest {

	@Mock
	private JavaMailSender mailSender;

	@Test
	void sendOrderEmail_shouldBuildConfirmationFromOrderEvent() throws Exception {
		OrderEmailListener listener = new OrderEmailListener(
				JsonMapper.builder().build(), mailSender, "toko@example.com");
		String payload = """
				{"orderId":42,"customerName":"Dzaki","recipientEmail":"dzaki@example.com",
				"productName":"Buku","quantity":2,"unitPrice":15000.00,"total":30000.00,
				"orderDate":"2026-10-09"}
				""";

		listener.sendOrderEmail(payload);

		ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
		verify(mailSender).send(messageCaptor.capture());
		SimpleMailMessage sentMessage = messageCaptor.getValue();
		assertEquals("toko@example.com", sentMessage.getFrom());
		assertEquals("dzaki@example.com", sentMessage.getTo()[0]);
		assertEquals("Konfirmasi Order #42", sentMessage.getSubject());
		org.junit.jupiter.api.Assertions.assertTrue(sentMessage.getText().contains("Total: 30000"));
	}
}