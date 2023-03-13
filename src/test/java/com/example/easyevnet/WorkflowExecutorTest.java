package com.example.easyevnet;

import com.example.app.bussines.BusinessModel;
import com.example.app.bussines.ShopEventType;
import com.example.easyevnet.monitor.api.model.ResponseList;
import com.example.easyevnet.monitor.audit.database.StatePersistenceRepository;
import com.example.easyevnet.monitor.audit.database.StatePersistenceService;
import com.example.easyevnet.monitor.audit.database.model.OrchestraPersistence;
import com.example.easyevnet.monitor.audit.database.model.StagePersistence;
import com.example.easyevnet.orchestra.builder.OrchestraBuilder;
import com.example.easyevnet.orchestra.orchestra.model.OrchestraData;
import com.example.easyevnet.orchestra.orchestra.model.OrchestraStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
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
    private StatePersistenceService<String> statePersistenceServiceImpl;

    @BeforeEach
    void setUp() {
        kafkaTemplate = getKafkaTemplate();
    }

    @Test
    void startOrderedWorkflow() throws Exception {
//        You need to have local kafka to run this test
        WorkflowContainer<String> executor = new WorkflowContainer<>(getKafkaConsumerProperties(), statePersistenceServiceImpl);

        executor.startOrderedWorkflow("1001", getOrchestra());

        TimeUnit.SECONDS.sleep(20);

        sendMessageWithDelay("com.example.app.bussines.ShopEventType.CREATE_ORDER", "CREATE_ORDER");
        TimeUnit.SECONDS.sleep(10);
        sendMessageWithDelay("com.example.app.bussines.ShopEventType.CHECK_PAYMENT", "CHECK_PAYMENT");
        TimeUnit.SECONDS.sleep(5);
        sendMessageWithDelay("com.example.app.bussines.ShopEventType.CANCEL_ORDER", "CANCEL_ORDER");


        // All states are done (DB)
        await().atMost(30, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        assertEquals(Set.of("DONE"), repository.findAllByBusinessId("1001").stream().map(StagePersistence::getStatus).collect(Collectors.toSet())));


        // GET
        // All states are done (REST)
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/v1/easyevent/monitor/stage"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        ResponseList<StagePersistence> actual = mapFromJson(response.body(), new TypeReference<>() {});
        assertEquals(3, actual.getElements().size());
        assertEquals(List.of("DONE", "DONE", "DONE"), actual.getElements().stream().map(StagePersistence::getStatus).collect(Collectors.toList()));
        assertEquals(List.of("1001", "1001", "1001"), actual.getElements().stream().map(StagePersistence::getBusinessId).collect(Collectors.toList()));

        // GET
        // Orchestra is done (REST)
        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/v1/easyevent/monitor/orchestra"))
                .GET()
                .build();

        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());

        ResponseList<OrchestraPersistence> actual2 = mapFromJson(response2.body(), new TypeReference<>() {});
        assertEquals(1, actual2.getElements().size());
        assertEquals(1001, Integer.parseInt(actual2.getElements().stream().findFirst().get().getBusinessId()));
        assertEquals(OrchestraStatus.DONE.name(), actual2.getElements().stream().findFirst().get().getStatus());
        assertEquals("localhost:29092", actual2.getElements().stream().findFirst().get().getBrokerUrl());
    }

    private void sendMessageWithDelay(String topic, String stage) {
        var record = new ProducerRecord<>(topic, "1001", mapToJson(new BusinessModel("Test")));
        record.headers()
                .add(new SingleRecordHeader("stage", stage.getBytes()));

        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        kafkaTemplate.send(record);
    }

    private void sendMessageWithDelayAsync(String topic, String stage) {
        var record = new ProducerRecord<>(topic, "1001", mapToJson(new BusinessModel("Test")));
        record.headers()
                .add(new SingleRecordHeader("stage", stage.getBytes()));

        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            return kafkaTemplate.send(record);
        });
    }

    private KafkaTemplate<String, String> getKafkaTemplate() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BROKER_URL);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(configProps));
    }

    private OrchestraData getOrchestra() {
        return new OrchestraBuilder()
                .stageInOrder(System.out::println, ShopEventType.CREATE_ORDER, BusinessModel.class)
                .onError(e -> System.out.println("ERROR in ShopEventType.CREATE_ORDER: " + e.getMessage()))
                .timeout(Duration.ofSeconds(10))
                .nextStage()
                .stageInOrder(System.out::println, ShopEventType.CHECK_PAYMENT, BusinessModel.class)
                .onError(e -> System.out.println("ERROR in ShopEventType.CHECK_PAYMENT: " + e.getMessage()))
                .timeout(Duration.ofSeconds(10))
                .nextStage()
                .stageInOrder(System.out::println, ShopEventType.CANCEL_ORDER, BusinessModel.class)
                .onError(e -> System.out.println("ERROR in ShopEventType.CANCEL_ORDER: " + e.getMessage()))
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

    protected <T> ResponseList<T> mapFromJson(String json, TypeReference<ResponseList<T>> clazz) {

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    protected <T> String mapToJson(T object) {

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}