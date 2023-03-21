package com.example.easyevnet.broker.kafka.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@RequiredArgsConstructor
public class KafkaListenerContainer<T, ID> {

    private final ConsumerFactory<T, ID> consumerFactory;
    private final ConcurrentLinkedQueue<ConcurrentMessageListenerContainer<?,?>> listener = new ConcurrentLinkedQueue<>();

    public KafkaListenerContainer(Properties kafkaListenerConfig) {
        this.consumerFactory = new DefaultKafkaConsumerFactory<>((Map<String, Object>) (Map) kafkaListenerConfig);
    }

    public ConcurrentMessageListenerContainer<T, ID> createStartedConsumer(Collection<String> topics, MessageListener<T, ID> messageConsumer) {
        ContainerProperties containerProps = new ContainerProperties(topics.toArray(new String[]{}));
        containerProps.setMessageListener(messageConsumer);

        ConcurrentMessageListenerContainer<T, ID> container = new ConcurrentMessageListenerContainer<>(consumerFactory, containerProps);

        log.info("container.start()");
        try {
//            https://stackoverflow.com/questions/37363119/kafka-producer-org-apache-kafka-common-serialization-stringserializer-could-no
            Thread.currentThread().setContextClassLoader(null);
            container.start();
        } catch (Throwable e) {
            log.error(String.valueOf(e));
        }
        log.info("container.start() end");

        listener.add(container);

        return container;
    }

    public void stopAllListeners() {
        listener.forEach(ConcurrentMessageListenerContainer::stop);
    }

}
