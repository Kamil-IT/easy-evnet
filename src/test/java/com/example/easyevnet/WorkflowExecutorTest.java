package com.example.easyevnet;

import com.example.app.bussines.ShopEventType;
import com.example.easyevnet.monitor.EventPublisher;
import com.example.easyevnet.orchestra.orchestra.builder.OrchestraBuilder;
import com.example.easyevnet.orchestra.orchestra.model.Orchestra;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer.SingleRecordHeader;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class WorkflowExecutorTest {

    @Spy
    ThreadPoolTaskExecutor spyTaskExecutor = new ThreadPoolTaskExecutor();


    private static final String BROKER_URL = "localhost:29092";

    private KafkaTemplate<String, String> kafkaTemplate;

    @BeforeEach
    void setUp() {
        kafkaTemplate = getKafkaTemplate();
    }

    @Test
    void startOrderedWorkflow() throws InterruptedException {
//        You need to have local kafka to run this test
        AtomicReference<String> actualMessage = new AtomicReference<>();
        WorkflowExecutor<String> executor = new WorkflowExecutor<>(getKafkaConsumerProperties());

        EventPublisher.getInstanceWorkflowFinished().subscribe(message -> {
            actualMessage.set(message.message());
        });

        executor.startOrderedWorkflow("1000", getOrchestra());

        TimeUnit.SECONDS.sleep(20);

        sendMessageWithDelay("com.example.app.bussines.ShopEventType.CREATE_ORDER", "CREATE_ORDER");
        TimeUnit.SECONDS.sleep(10);
        sendMessageWithDelay("com.example.app.bussines.ShopEventType.CHECK_PAYMENT", "CHECK_PAYMENT");
        TimeUnit.SECONDS.sleep(5);
        sendMessageWithDelay("com.example.app.bussines.ShopEventType.CANCEL_ORDER", "CANCEL_ORDER");


        await().atMost(30, TimeUnit.SECONDS)
                .untilAsserted(() -> assertEquals("Stage completed. Id: 1000", actualMessage.get()));
    }

    private void sendMessageWithDelay(String topic, String stage) {
        var record = new ProducerRecord<>(topic, "1001", "Hello World");
        record.headers()
                .add(new SingleRecordHeader("stage", stage.getBytes()));

        kafkaTemplate.send(record);
    }

    private KafkaTemplate<String, String> getKafkaTemplate() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BROKER_URL);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(configProps));
    }

    private Orchestra getOrchestra() {
        return new OrchestraBuilder()
                .addStagesInOrder(System.out::println, ShopEventType.CREATE_ORDER)
                .onError(e -> System.out.println("ERROR in ShopEventType.CREATE_ORDER"))
                .timeout(Duration.ofSeconds(10))
                .nextStage()
                .addStagesInOrder(System.out::println, ShopEventType.CHECK_PAYMENT)
                .onError(e -> System.out.println("ERROR in ShopEventType.CHECK_PAYMENT"))
                .timeout(Duration.ofSeconds(10))
                .nextStage()
                .addStagesInOrder(System.out::println, ShopEventType.CANCEL_ORDER)
                .onError(e -> System.out.println("ERROR in ShopEventType.CANCEL_ORDER"))
                .timeout(Duration.ofSeconds(10))
                .build();
    }

    private Properties getKafkaConsumerProperties() {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BROKER_URL);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "KafkaExampleConsumer");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1);

        return props;
    }
}