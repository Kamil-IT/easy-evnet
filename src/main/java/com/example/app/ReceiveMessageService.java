package com.example.app;

import com.example.app.bussines.ShopEventType;
import com.example.easyevnet.WorkflowExecutor;
import com.example.easyevnet.orchestra.builder.OrchestraBuilder;
import com.example.easyevnet.orchestra.orchestra.model.Orchestra;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Duration;

@Profile("!test")
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ReceiveMessageService {

    private final WorkflowExecutor workflowExecutor;

    @PostConstruct
    public void initializeOrchestra() {
        workflowExecutor.startOrderedWorkflow(123, orchestra());
    }

    private Orchestra orchestra() {
        return new OrchestraBuilder()
                .stageInOrder(log::info, ShopEventType.CREATE_ORDER)
                    .onError(e -> log.error("ERROR in ShopEventType.CREATE_ORDER"))
                    .timeout(Duration.ofSeconds(10))
                    .waitForResponse((r) -> System.out.println("then"))
                    .afterProcessMessage((r) -> System.out.println("then"))
                    .nextStage()
                .stageInOrder(log::info, ShopEventType.CHECK_PAYMENT)
                    .onError(e -> log.error("ERROR in ShopEventType.CHECK_PAYMENT"))
                    .timeout(Duration.ofSeconds(10))
                    .nextStage()
                    .stageInOrder(log::info, ShopEventType.CANCEL_ORDER)
                    .onError(e -> log.error("ERROR in ShopEventType.CANCEL_ORDER"))
                    .timeout(Duration.ofSeconds(10))
                .build();
    }
}
