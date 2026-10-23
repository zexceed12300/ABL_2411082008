package com.ihsan.order.query;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.rabbitmq.listener.simple.auto-startup=false",
		"eureka.client.enabled=false"
})
class OrderQueryApplicationTests {

	@Test
	void contextLoads() {
	}

}
