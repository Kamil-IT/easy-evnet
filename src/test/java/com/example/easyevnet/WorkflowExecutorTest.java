package com.example.easyevnet;

import com.example.app.bussines.ShopEventType;
import com.example.easyevnet.monitor.event.EventPublisher;
import com.example.easyevnet.monitor.api.model.GetStages;
import com.example.easyevnet.orchestra.builder.OrchestraBuilder;
import com.example.easyevnet.orchestra.database.StatePersistence;
import com.example.easyevnet.orchestra.database.StatePersistenceRepository;
import com.example.easyevnet.orchestra.database.StatePersistenceService;
import com.example.easyevnet.orchestra.orchestra.model.Orchestra;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer.SingleRecordHeader;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ExtendWith(MockitoExtension.class)
class WorkflowExecutorTest {

    private static final String BROKER_URL = "localhost:29092";

    @Spy
    ThreadPoolTaskExecutor spyTaskExecutor = new ThreadPoolTaskExecutor();

    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private StatePersistenceRepository repository;
    @Autowired
    private StatePersistenceService statePersistenceService;

    @BeforeEach
    void setUp() {
        kafkaTemplate = getKafkaTemplate();
    }

    @Test
    void startOrderedWorkflow() throws Exception {
//        You need to have local kafka to run this test
        AtomicReference<String> actualMessage = new AtomicReference<>();
        WorkflowExecutor<String> executor = new WorkflowExecutor<>(getKafkaConsumerProperties(), statePersistenceService);

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

        assertEquals(Set.of("DONE"), repository.findAllByBusinessId("1000").stream().map(StatePersistence::getStatus).collect(Collectors.toSet()));



        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/v1/easyevent/monitor/stage"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        GetStages actual = mapFromJson(response.body(), GetStages.class);
        assertEquals(3, actual.getStages().size());
        assertEquals(List.of("DONE", "DONE", "DONE"), actual.getStages().stream().map(StatePersistence::getStatus).collect(Collectors.toList()));
        assertEquals(List.of("1000", "1000", "1000"), actual.getStages().stream().map(StatePersistence::getBusinessId).collect(Collectors.toList()));

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
                .stageInOrder(System.out::println, ShopEventType.CREATE_ORDER)
                .onError(e -> System.out.println("ERROR in ShopEventType.CREATE_ORDER"))
                .timeout(Duration.ofSeconds(10))
                .nextStage()
                .stageInOrder(System.out::println, ShopEventType.CHECK_PAYMENT)
                .onError(e -> System.out.println("ERROR in ShopEventType.CHECK_PAYMENT"))
                .timeout(Duration.ofSeconds(10))
                .nextStage()
                .stageInOrder(System.out::println, ShopEventType.CANCEL_ORDER)
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

    protected <T> T mapFromJson(String json, Class<T> clazz) {

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}