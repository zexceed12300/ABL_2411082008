package com.ihsan.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class ConsumerService {

    @RabbitListener(queues = "MyQueue")
    public void receiveMessage(String message) {
        System.out.println("Received: " + message);
    }
}