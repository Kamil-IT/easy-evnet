package com.example.easyevnet;

import com.example.easyevnet.broker.kafka.config.KafkaContainerFactory;
import com.example.easyevnet.orchestra.OrchestraContainerFactory;
import com.example.easyevnet.orchestra.OrchestraContainer;
import com.example.easyevnet.orchestra.StageExecutor;
import com.example.easyevnet.orchestra.database.StatePersistenceService;
import com.example.easyevnet.orchestra.orchestra.model.OrchestraData;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class WorkflowContainer<ID> {

    //    TODO: Dodaj rejest wszystkich workflow bazuacy na eventach i pozniej dodaj to do api

    private final Map<ID, CompletableFuture<OrchestraContainer<ID>>> threads = new ConcurrentHashMap<>();

    private final OrchestraContainerFactory<ID> orchestraContainerFactory;

    public WorkflowContainer(Properties listenerConfig, StatePersistenceService statePersistenceService) {
        this.orchestraContainerFactory = new OrchestraContainerFactory<>(new KafkaContainerFactory(listenerConfig), statePersistenceService);
    }

    public void startOrderedWorkflow(ID singleWorkflowId, OrchestraData orchestraData) {
        addStageExecutor(singleWorkflowId, orchestraData);
        addStageToDb(singleWorkflowId, orchestraData);

        CompletableFuture<OrchestraContainer<ID>> async = CompletableFuture.supplyAsync(orchestraContainerFactory::startOrchestra);

        addMonitoring(singleWorkflowId, async);
    }

    private void addStageToDb(ID singleWorkflowId, OrchestraData orchestraData) {
//        Implement
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
