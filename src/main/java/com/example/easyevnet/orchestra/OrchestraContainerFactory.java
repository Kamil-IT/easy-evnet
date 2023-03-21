package com.example.easyevnet.orchestra;

import com.example.easyevnet.broker.kafka.config.KafkaContainerFactory;
import com.example.easyevnet.monitor.audit.database.StatePersistenceService;
import com.example.easyevnet.orchestra.stage.StageExecutor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@RequiredArgsConstructor
public class OrchestraContainerFactory<ID> {

    private final ConcurrentLinkedQueue<OrchestraContainer<ID>> listeners = new ConcurrentLinkedQueue<>();

    private final KafkaContainerFactory kafkaContainerFactory;
    private final StatePersistenceService<ID> statePersistenceService;
    private StageExecutor<ID> stageExecutor;
    private final ObjectMapper objectMapper;

    public OrchestraContainer<ID> startOrchestra() {
        OrchestraContainer<ID> container = new OrchestraContainer<>(stageExecutor, kafkaContainerFactory, statePersistenceService, objectMapper);
        listeners.add(container);

        log.info("container.startOrchestra()");
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
