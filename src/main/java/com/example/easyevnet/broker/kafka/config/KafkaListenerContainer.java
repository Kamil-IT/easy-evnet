package com.example.easyevnet.broker.kafka.config;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;

@RequiredArgsConstructor
public class KafkaListenerContainer<T, ID> {

    private final ConsumerFactory<T, ID> consumerFactory;
    private final ConcurrentLinkedQueue<KafkaMessageListenerContainer<?,?>> listener = new ConcurrentLinkedQueue<>();

    public KafkaListenerContainer(Properties kafkaListenerConfig) {
        this.consumerFactory = new DefaultKafkaConsumerFactory<>((Map<String, Object>) (Map) kafkaListenerConfig);
    }

    public KafkaMessageListenerContainer<T, ID> createStartedConsumer(Collection<String> topics, MessageListener<T, ID> messageConsumer) {
        ContainerProperties containerProps = new ContainerProperties(topics.toArray(new String[]{}));
        containerProps.setMessageListener(messageConsumer);

        KafkaMessageListenerContainer<T, ID> container = new KafkaMessageListenerContainer<>(consumerFactory, containerProps);

        container.start();

        listener.add(container);

        return container;
    }

    public void stopAllListeners() {
        listener.forEach(KafkaMessageListenerContainer::stop);
    }

}
