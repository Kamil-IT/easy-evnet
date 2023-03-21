package com.example.easyevnet;

import com.example.easyevnet.broker.kafka.config.KafkaContainerFactory;
import com.example.easyevnet.orchestra.OrchestraContainer;
import com.example.easyevnet.orchestra.OrchestraContainerFactory;
import com.example.easyevnet.monitor.audit.database.model.OrchestraPersistence;
import com.example.easyevnet.orchestra.orchestra.model.OrchestraStatus;
import com.example.easyevnet.monitor.audit.database.StatePersistenceService;
import com.example.easyevnet.orchestra.orchestra.model.OrchestraData;
import com.example.easyevnet.orchestra.stage.StageExecutor;
import com.example.easyevnet.orchestra.stage.model.Stage;
import com.example.easyevnet.orchestra.stage.model.StageData;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.example.easyevnet.TypeUtil.objectMapper;

public class WorkflowContainer<ID> {

    //    TODO: Dodaj rejest wszystkich workflow bazuacy na eventach i pozniej dodaj to do api

    private final Map<ID, CompletableFuture<OrchestraContainer<ID>>> threads = new ConcurrentHashMap<>();

    private final OrchestraContainerFactory<ID> orchestraContainerFactory;
    private final KafkaContainerFactory kafkaContainerFactory;
    private final StatePersistenceService<ID> statePersistenceService;

    public WorkflowContainer(Properties listenerConfig, StatePersistenceService<ID> statePersistenceService) {
        this.kafkaContainerFactory = new KafkaContainerFactory(listenerConfig);
        this.orchestraContainerFactory = new OrchestraContainerFactory<>(kafkaContainerFactory, statePersistenceService, objectMapper());
        this.statePersistenceService = statePersistenceService;
    }

    public WorkflowContainer(Properties listenerConfig, StatePersistenceService<ID> statePersistenceService, ObjectMapper objectMapper) {
        this.kafkaContainerFactory = new KafkaContainerFactory(listenerConfig);
        this.orchestraContainerFactory = new OrchestraContainerFactory<>(kafkaContainerFactory, statePersistenceService, objectMapper);
        this.statePersistenceService = statePersistenceService;
    }

    public void startOrderedWorkflow(ID singleWorkflowId, OrchestraData orchestraData) {
        addStageExecutor(singleWorkflowId, orchestraData);
        addStageToDb(singleWorkflowId, orchestraData);

        CompletableFuture<OrchestraContainer<ID>> async = CompletableFuture.supplyAsync(orchestraContainerFactory::startOrchestra);

        addMonitoring(singleWorkflowId, async);
    }

    private void addStageToDb(ID singleWorkflowId, OrchestraData orchestraData) {
        statePersistenceService.saveOrchestra(
                OrchestraPersistence.builder()
                        .stagesBraking(getStagesNames(orchestraData.getStagesBrakingOrder()))
                        .stagesInOrder(getStagesNames(orchestraData.getStagesInOrder()))
                        .sagesDefault(getStagesNames(orchestraData.getStageDefault()))
                        .businessId(singleWorkflowId.toString())
                        .brokerUrl(kafkaContainerFactory.getBrokerUrl())
                        .status(OrchestraStatus.PROCESSING.toString())
                        .build());
    }

    @Nullable
    private static String getStagesNames(List<Stage<?>> orchestraData) {
        String namesCollected = orchestraData.stream()
                .map(Stage::name)
                .collect(Collectors.joining(", "));
        return namesCollected.isBlank() ? null : namesCollected;
    }

    private void addStageExecutor(ID singleWorkflowId, OrchestraData orchestraData) {
        if (!orchestraContainerFactory.isStateExecutorSet()) {
            StageExecutor<ID> stageExecutor = new StageExecutor<>(orchestraData, singleWorkflowId);
            orchestraContainerFactory.setTateExecutor(stageExecutor);
        }
    }

    private void addMonitoring(ID singleWorkflowId, CompletableFuture<OrchestraContainer<ID>> async) {
        threads.put(singleWorkflowId, async);
        async.thenApply(i -> threads.remove(singleWorkflowId));
    }

}
