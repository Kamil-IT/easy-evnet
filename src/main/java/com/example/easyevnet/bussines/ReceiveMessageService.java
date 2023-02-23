package com.example.easyevnet.bussines;

import com.example.easyevnet.saga.source.stage.StateManager;
import com.example.easyevnet.saga.source.stage.model.Orchestra;
import com.example.easyevnet.saga.source.stage.model.builder.OrchestraBuilder;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Slf4j
@Configuration
public class ReceiveMessageService {

    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @PostConstruct
    public void initializeOrchestra() {
        StateManager.startOrderedWorkflow(orchestra(), 123);
    }

    public Orchestra orchestra() {
        return new OrchestraBuilder()
                    .addStagesInOrder(log::info, ShopEventType.CREATE_ORDER)
                        .onError(() -> log.error("ERROR in ShopEventType.CREATE_ORDER"))
                        .timeout(Duration.ofSeconds(10))
                .nextStage()
                    .addStagesInOrder(log::info, ShopEventType.CANCEL_ORDER)
                        .onError(() -> log.error("ERROR in ShopEventType.CANCEL_ORDER"))
                        .timeout(Duration.ofSeconds(10))
                .build();
    }
}
