package com.example.easyevnet.broker.kafka.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class KafkaListenerConfig {

    private final String broker;
    private final ConsumerFactory<String, String> consumerFactory;

    public KafkaListenerConfig(String broker, Properties kafkaProperties) {
        this.broker = broker;
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
