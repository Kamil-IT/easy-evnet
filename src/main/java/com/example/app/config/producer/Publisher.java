package com.example.app.config.producer;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class Publisher {

    public static final String TOPIC_NAME = "test.topic";
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String topic, String msg) {
        kafkaTemplate.send(topic, "1001", msg);
    }
}
