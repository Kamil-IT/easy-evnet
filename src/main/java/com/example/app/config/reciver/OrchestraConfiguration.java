package com.example.app.config.reciver;

import com.example.easyevnet.WorkflowContainer;
import com.example.easyevnet.monitor.audit.database.StatePersistenceService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Properties;

@Profile("!test")
@Configuration
public class OrchestraConfiguration {

    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String brokerUrl;

    @Bean
    WorkflowContainer<Integer> orchestraExecutor(StatePersistenceService statePersistenceServiceImpl) {
        return new WorkflowContainer<>(kafkaProperties(), statePersistenceServiceImpl);
    }


    private Properties kafkaProperties() {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrl);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "KafkaExampleConsumer");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1);

        return props;
    }
}
