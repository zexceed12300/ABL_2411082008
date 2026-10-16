package com.ihsan.producer;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service 
public class ProducerService {
    @Autowired 
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Queue queue;

    public void sendMessage(String message) {
        rabbitTemplate.convertAndSend(queue.getName(), message);
        System.out.println("Sent message: " + message);
    }
}
