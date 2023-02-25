package com.example.easyevnet;

import com.example.easyevnet.monitor.EventPublisher;
import com.example.easyevnet.broker.kafka.config.KafkaListenerConfig;
import com.example.easyevnet.orchestra.OrchestraExecutor;
import com.example.easyevnet.orchestra.StageExecutor;
import com.example.easyevnet.orchestra.orchestra.model.Orchestra;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class WorkflowExecutor<ID> {

    private final Map<ID, CompletableFuture<Boolean>> threads = new ConcurrentHashMap<>();
    private final KafkaListenerConfig kafkaListenerConfig;

    public WorkflowExecutor(Properties kafkaProperties) {
        this.kafkaListenerConfig = new KafkaListenerConfig(kafkaProperties);
    }

    public void startOrderedWorkflow(ID workflowIdentifier, Orchestra orchestra) {
        StageExecutor<ID> stageExecutor = new StageExecutor<>(orchestra, workflowIdentifier);
        OrchestraExecutor<ID> orchestraExecutor = new OrchestraExecutor<>(stageExecutor, this.kafkaListenerConfig);

        CompletableFuture<Boolean> async = CompletableFuture.supplyAsync(orchestraExecutor::startOrchestra);
        threads.put(workflowIdentifier, async);
        async.thenApply(i -> threads.remove(workflowIdentifier));
    }
}
