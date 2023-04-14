package com.example.easyevnet.broker.kafka.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.kafka.listener.MessageListener;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class KafkaContainerFactory {

    private final Map<Collection<String>, KafkaListenerContainer<?, ?>> listeners = new ConcurrentHashMap<>();
    private final Properties kafkaListenerConfig;

    public <T, ID> KafkaListenerContainer<T, ID> createStartedConsumer(Set<String> topics, MessageListener<T, ID> messageConsumer) {
        KafkaListenerContainer<T, ID> listener = new KafkaListenerContainer<>(kafkaListenerConfig);
        listeners.computeIfAbsent(topics, (e) -> {
            listener.createStartedConsumer(topics, messageConsumer);
            return listener;
        });
        return listener;
    }

    public String getBrokerUrl() {
        return kafkaListenerConfig.getProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG);
    }

    public void stopAllListeners() {
        listeners.forEach((key, value) -> value.stopAllListeners());
    }
}
