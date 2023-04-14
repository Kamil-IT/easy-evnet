package com.example.easyevnet.orchestra;

import com.example.easyevnet.broker.kafka.config.KafkaContainerFactory;
import com.example.easyevnet.monitor.audit.database.StatePersistenceService;
import com.example.easyevnet.monitor.audit.database.model.StagePersistence;
import com.example.easyevnet.orchestra.orchestra.model.StageStatus;
import com.example.easyevnet.orchestra.orchestra.model.StageType;
import com.example.easyevnet.orchestra.stage.StageExecutor;
import com.example.easyevnet.orchestra.stage.model.Stage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.StreamSupport;

import static com.example.easyevnet.TypeUtil.isCommonJavaType;

@Slf4j
@RequiredArgsConstructor
public class OrchestraContainer<ID> {

    private final StageExecutor<ID> stageExecutor;
    private final KafkaContainerFactory kafkaContainerFactory;
    private final StatePersistenceService<ID> statePersistenceService;
    private final ObjectMapper objectMapper;

    public void startOrchestra() {
        List<String> topics = stageExecutor.getTopics();

        kafkaContainerFactory.createStartedConsumer(new HashSet<>(topics), this::processNextStep);
    }

    private void processNextStep(ConsumerRecord<ID, String> rec) {
        ID id = rec.key();
        String idString = getIdString(id);

        Stage<?> stage = getStage(rec);
        String stageName = stage.stageData().name();
        String topic = rec.topic();

        log.info("Processing " + idString + ", " + stage.name());

        if (isOrchestraStarted(id) && isStageValid(id, stage, topic)) {
            notifyStageIsProcessing(idString, stage, topic);

            StageStatus status = processStage(rec, stage);

            if (StageStatus.DONE.equals(status)) {
                statePersistenceService.finishProcessing(id, stageName, topic);

            } else if (StageStatus.ERROR.equals(status)) {
                log.error("Stage processed with error");
                statePersistenceService.markProcessAsError(id, stageName, topic, "e.getMessage() - fix me");
            }

            if (stageExecutor.getOrchestraData().getLastStage().equals(stage)) {
                statePersistenceService.finishOrchestra(id);
            }
        }
    }

    private boolean isOrchestraStarted(ID id) {
        boolean orchestraStarted = statePersistenceService.isOrchestraStarted(id);
        if (!orchestraStarted) {
            log.error("Orchestra with Id: " + id + " not started. You are inside orchestra with id: " + stageExecutor.getWorkflowIdentifier());
        }
        return orchestraStarted;
    }

    private String getIdString(ID id) {
        String idString = "Error";

        if (isCommonJavaType(id)) {
            idString = String.valueOf(id);
        } else {
//            TODO: Add convert from json
        }
        return idString;
    }

    private <T> StageStatus processStage(ConsumerRecord<ID, String> rec, Stage<T> stage) {
        if (StageType.ORDERED.equals(stage.stageType())) {
            return stageExecutor.processNextOrderedStage(stage, getBody(rec, stage.stageData().bodyClass()));
        } else if (StageType.BRAKING.equals(stage.stageType())) {
            return stageExecutor.processNextBrakingStage(stage, getBody(rec, stage.stageData().bodyClass()));
        } else {
            return stageExecutor.processNextDefaultStage(stage, getBody(rec, stage.stageData().bodyClass()));
        }
    }

    private <T> T getBody(ConsumerRecord<ID, String> rec, Class<T> bodyClass) {
        try {
            return objectMapper.readValue(rec.value(), bodyClass);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void notifyStageIsProcessing(String id, Stage<?> stage, String topic) {
        statePersistenceService.saveState(StagePersistence.builder()
                .businessId(id)
                .topic(topic)
                .status(StageStatus.PROCESSING.toString())
                .stageType(stage.stageType())
                .stageName(stage.stageData().name())
                .build());
    }

    private boolean isStageValid(ID id, Stage<?> stage, String topic) {
        List<String> allStagesName = stageExecutor.getOrchestraData().getAllStagesName();
        String name = stage.stageData().name();
        Map<StageType, Set<String>> processedStages = statePersistenceService.getProcessedStagesByType(id, topic);

        if (!allStagesName.contains(name)) {
            log.error("Stage not exist: [" + id + ", " + stage + "]");
            return false;

        } else if (StageType.ORDERED.equals(stage.stageType()) &&
                processedStages.getOrDefault(StageType.ORDERED, new HashSet<>()).stream().anyMatch(stageName -> stageName.equals(name)) &&
                stageExecutor.getOrchestraData().getOrderedNextStage(stage).filter(s -> s.equals(stage)).isPresent()) {
            log.error("Ordered stage already completed or in process: [" + id + ", " + stage + "]");
            return false;

        } else if (StageType.BRAKING.equals(stage.stageType()) && processedStages.getOrDefault(StageType.BRAKING, new HashSet<>()).stream().anyMatch(stageName -> stageName.equals(name))) {
            log.error("Braking stage already completed or in process: [" + id + ", " + stage + "]");
            return false;

        } else if (StageType.DEFAULT.equals(stage.stageType()) && processedStages.getOrDefault(StageType.DEFAULT, new HashSet<>()).stream().anyMatch(stageName -> stageName.equals(name))) {
            log.error("Default stage already completed or in process: [" + id + ", " + stage + "]");
            return false;

        }
        return true;
    }

    private Stage<?> getStage(ConsumerRecord<ID, String> rec) {
        String stageName = extractStageNameFromHeader(rec);

        return stageExecutor.getStageByName(stageName);
    }

    private String extractStageNameFromHeader(ConsumerRecord<ID, String> rec) {
        return StreamSupport.stream(rec.headers().headers("stage").spliterator(), false)
                .findFirst()
                .map(Header::value)
                .map(stage -> new String(stage, StandardCharsets.UTF_8))
                .orElseThrow();
    }
}
