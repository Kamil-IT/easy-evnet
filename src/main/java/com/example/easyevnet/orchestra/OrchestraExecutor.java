package com.example.easyevnet.orchestra;

import com.example.easyevnet.broker.kafka.config.KafkaListenerConfig;
import com.example.easyevnet.orchestra.database.StatePersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.StreamSupport;

@Slf4j
@RequiredArgsConstructor
public class OrchestraExecutor<ID> {

    private final StageExecutor<ID> stageExecutor;
    private final KafkaListenerConfig kafkaListenerConfig;
    private final StatePersistenceService statePersistenceService;

//    This step should be in container not here
    public boolean startOrchestra() {
        List<String> topics = stageExecutor.getTopics();

        kafkaListenerConfig.createStartedConsumer(topics, this::processNextStep);

        return true;
    }

    private void processNextStep(ConsumerRecord<String, String> rec) {
        String id = (String) stageExecutor.getWorkflowIdentifier();
        String stage = getStage(rec);
        String topic = rec.topic();

//        Should not add object if stage is not able to perform
        boolean ableToProcessIfYesStart = statePersistenceService.isAbleToProcessIfYesStart(id, stage, topic);

        if (ableToProcessIfYesStart) {
            try {
                boolean status = stageExecutor.processNextStep(stage, rec.value());
                if (!status) {
                    throw new IllegalArgumentException("Status processed with errors");
                }
            } catch (Exception e) {
                statePersistenceService.markProcessAsError(id, stage, e.getMessage(), topic);
            }

            statePersistenceService.finishProcessing(id, stage, topic);
        } else {
            statePersistenceService.markProcessAsError(id, stage, "Stage already done", topic);
            log.error("stage already processed");
        }


    }

    private static String getStage(ConsumerRecord<String, String> rec) {
        return StreamSupport.stream(rec.headers().headers("stage").spliterator(), false)
                .findFirst()
                .map(Header::value)
                .map(stage -> new String(stage, StandardCharsets.UTF_8))
                .orElse("CHECK_PAYMENT");
    }
}
