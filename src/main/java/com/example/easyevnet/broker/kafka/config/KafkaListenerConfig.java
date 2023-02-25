package com.example.easyevnet.broker.kafka.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.*;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

public class KafkaListenerConfig {

    private final ConsumerFactory<String, String> consumerFactory;

    public KafkaListenerConfig(Properties kafkaProperties) {
        this.consumerFactory = new DefaultKafkaConsumerFactory<>((Map<String, Object>) (Map) kafkaProperties);
    }

    public KafkaMessageListenerContainer<String, String> createStartedConsumer(Collection<String> topics, MessageListener<String, String> messageConsumer) {
        ContainerProperties containerProps = new ContainerProperties(topics.toArray(new String[]{}));
        containerProps.setMessageListener(messageConsumer);

        KafkaMessageListenerContainer<String, String> container = new KafkaMessageListenerContainer<>(consumerFactory, containerProps);

        container.start();

        return container;
    }
}
