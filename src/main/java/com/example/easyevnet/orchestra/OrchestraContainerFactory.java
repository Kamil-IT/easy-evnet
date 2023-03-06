package com.example.easyevnet.orchestra;

import com.example.easyevnet.broker.kafka.config.KafkaContainerFactory;
import com.example.easyevnet.broker.kafka.config.KafkaListenerContainer;
import com.example.easyevnet.orchestra.database.StatePersistenceService;
import com.example.easyevnet.orchestra.orchestra.model.OrchestraData;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

@RequiredArgsConstructor
public class OrchestraContainerFactory<ID> {

    private final ConcurrentLinkedQueue<OrchestraContainer<ID>> listeners = new ConcurrentLinkedQueue<>();

    private final KafkaContainerFactory kafkaContainerFactory;
    private final StatePersistenceService statePersistenceService;
    private StageExecutor<ID> stageExecutor;

    public OrchestraContainer<ID> startOrchestra() {
        OrchestraContainer<ID> container = new OrchestraContainer<>(stageExecutor, kafkaContainerFactory, statePersistenceService);
        listeners.add(container);

        container.startOrchestra();

        return container;
    }

    public void setTateExecutor(StageExecutor<ID> stageExecutor) {
        this.stageExecutor = stageExecutor;
    }

    public boolean isStateExecutorSet(){
        return this.stageExecutor != null;
    }
}