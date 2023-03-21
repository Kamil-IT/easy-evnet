package com.example.easyevnet.broker.kafka.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;

import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;

@RequiredArgsConstructor
public class KafkaContainerFactory {

    private final ConcurrentLinkedQueue<KafkaListenerContainer<?,?>> listeners = new ConcurrentLinkedQueue<>();
    private final Properties kafkaListenerConfig;

    public <T, ID> ConcurrentMessageListenerContainer<T, ID> createStartedConsumer(Collection<String> topics, MessageListener<T, ID> messageConsumer) {
        KafkaListenerContainer<T, ID> listener = new KafkaListenerContainer<>(kafkaListenerConfig);

        return listener.createStartedConsumer(topics, messageConsumer);
    }

    public String getBrokerUrl() {
        return kafkaListenerConfig.getProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG);
    }

    public void stopAllListeners() {
        listeners.forEach(KafkaListenerContainer::stopAllListeners);
    }
}
