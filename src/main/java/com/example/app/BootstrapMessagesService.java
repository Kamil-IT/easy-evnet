package com.example.app;

import com.example.app.bussines.BusinessModel;
import com.example.app.bussines.ShopEventType;
import com.example.easyevnet.WorkflowContainer;
import com.example.easyevnet.orchestra.builder.OrchestraBuilder;
import com.example.easyevnet.orchestra.orchestra.model.OrchestraData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer.SingleRecordHeader;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Profile("!test")
@RequiredArgsConstructor
@EnableScheduling
@Slf4j
@Configuration
public class BootstrapMessagesService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final WorkflowContainer<String> orchestraExecutor;

    @Scheduled(cron = "* * * * * *")
    @PostConstruct
    public void publishMessage() throws InterruptedException {
        String id = String.valueOf(ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE));
        orchestraExecutor.startOrderedWorkflow(id, orchestra());
        log.info("Started: " + id);

        var record = new ProducerRecord<>("com.example.app.bussines.ShopEventType.CREATE_ORDER", id, mapToJson(new BusinessModel("Test")));
        record.headers()
                .add(new SingleRecordHeader("stage", "CREATE_ORDER".getBytes()));
        kafkaTemplate.send(record);
        log.info("Send: " + "CREATE_ORDER");

        TimeUnit.SECONDS.sleep(1);


        var record2 = new ProducerRecord<>("com.example.app.bussines.ShopEventType.CHECK_PAYMENT", id, mapToJson(new BusinessModel("Test")));
        record2.headers()
                .add(new SingleRecordHeader("stage", "CHECK_PAYMENT".getBytes()));
        kafkaTemplate.send(record2);
        log.info("Send: " + "CHECK_PAYMENT");

        TimeUnit.SECONDS.sleep(1);

        var record3 = new ProducerRecord<>("com.example.app.bussines.ShopEventType.CANCEL_ORDER", id, mapToJson(new BusinessModel("Test")));
        record3.headers()
                .add(new SingleRecordHeader("stage", "CANCEL_ORDER".getBytes()));
        kafkaTemplate.send(record3);
        log.info("Send: " + "CANCEL_ORDER");
    }

    private OrchestraData orchestra() {
        return new OrchestraBuilder()
                .stageInOrder(System.out::println, ShopEventType.CREATE_ORDER, BusinessModel.class)
                .onError(e -> log.error("ERROR in ShopEventType.CREATE_ORDER"))
                .timeout(Duration.ofSeconds(10))
                .waitForResponse((r) -> System.out.println("TODO: implement"))
                .afterProcessMessage((r) -> System.out.println("then"))
                .nextStage()
                .stageInOrder(System.out::println, ShopEventType.CHECK_PAYMENT, BusinessModel.class)
                .onError(e -> log.error("ERROR in ShopEventType.CHECK_PAYMENT"))
                .timeout(Duration.ofSeconds(10))
                .nextStage()
                .stageInOrder(System.out::println, ShopEventType.CANCEL_ORDER, BusinessModel.class)
                .onError(e -> log.error("ERROR in ShopEventType.CANCEL_ORDER"))
                .timeout(Duration.ofSeconds(10))
                .nextStage()
                .stage(System.out::println, ShopEventType.ADD_COMMENT, BusinessModel.class)
                .onError(e -> log.error("ERROR in ShopEventType.ADD_COMMENT"))
                .timeout(Duration.ofSeconds(10))
                .build();
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
