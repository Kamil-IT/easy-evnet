package com.example.easyevnet.orchestra.stage;

import com.example.easyevnet.broker.kafka.model.ReceivedMessage;
import com.example.easyevnet.monitor.api.event.EventPublisher;
import com.example.easyevnet.monitor.api.event.StageFinishedEvent;
import com.example.easyevnet.monitor.api.event.WorkflowFinishedWithErrorEvent;
import com.example.easyevnet.orchestra.orchestra.model.StageStatus;
import com.example.easyevnet.orchestra.orchestra.model.OrchestraData;
import com.example.easyevnet.orchestra.stage.model.Stage;
import com.example.easyevnet.orchestra.stage.processor.ProcessorOrderedStage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;

@Slf4j
public class StageExecutor<ID> {

    private final EventPublisher<StageFinishedEvent> publisherFinished;
    private final EventPublisher<WorkflowFinishedWithErrorEvent> publisherError;

    @Getter
    private final OrchestraData orchestraData;
    @Getter
    private final ID workflowIdentifier;

    public StageExecutor(OrchestraData orchestraData, ID workflowIdentifier) {
        this.orchestraData = orchestraData;
        this.workflowIdentifier = workflowIdentifier;
        this.publisherFinished = EventPublisher.getInstanceStageFinished();
        this.publisherError = EventPublisher.getInstanceFinishWithErrorWorkflow();
    }

    //    TODO: Not StageStatus add more complicated object for example message if fail
    public <E> StageStatus processNextOrderedStage(Stage<E> stage, String body) {

        StageStatus status = new ProcessorOrderedStage<>(stage)
                .processOrderStage(new ReceivedMessage<>(workflowIdentifier, body, new HashMap<>(), (Stage<String>) stage));

        if (StageStatus.DONE.equals(status)) {
            publisherFinished.publish(new StageFinishedEvent("Stage completed. Id: " + workflowIdentifier));
            log.info("Stage completed. Id: " + workflowIdentifier);

        } else if (StageStatus.ERROR.equals(status)) {
            publisherError.publish(new WorkflowFinishedWithErrorEvent("Stage completed. Id: " + workflowIdentifier));
            log.info("Stage completed. Id: " + workflowIdentifier);
        }

        return status;
    }


    public <E> StageStatus processNextDefaultStage(Stage<E> stage, String body) {

//        TODO: Implement
        StageStatus status = new ProcessorOrderedStage<>(stage)
                .processOrderStage(new ReceivedMessage<>(workflowIdentifier, body, new HashMap<>(), (Stage<String>) stage));

        if (StageStatus.DONE.equals(status)) {
            publisherFinished.publish(new StageFinishedEvent("Stage completed. Id: " + workflowIdentifier));
            log.info("Stage completed. Id: " + workflowIdentifier);

        } else if (StageStatus.ERROR.equals(status)) {
            publisherError.publish(new WorkflowFinishedWithErrorEvent("Stage completed. Id: " + workflowIdentifier));
            log.info("Stage completed. Id: " + workflowIdentifier);
        }

        return status;
    }

    public <E> StageStatus processNextBrakingStage(Stage<E> stage, String body) {
        //        TODO: Implement

        StageStatus status = new ProcessorOrderedStage<>(stage)
                .processOrderStage(new ReceivedMessage<>(workflowIdentifier, body, new HashMap<>(), (Stage<String>) stage));

        if (StageStatus.DONE.equals(status)) {
            publisherFinished.publish(new StageFinishedEvent("Stage completed. Id: " + workflowIdentifier));
            log.info("Stage completed. Id: " + workflowIdentifier);

        } else if (StageStatus.ERROR.equals(status)) {
            publisherError.publish(new WorkflowFinishedWithErrorEvent("Stage completed. Id: " + workflowIdentifier));
            log.info("Stage completed. Id: " + workflowIdentifier);
        }

        return status;
    }

    public Stage<?> getStageByName(String stageName) {
        return orchestraData.getAllStages()
                .stream()
                .filter(s -> s.stageData().name().equals(stageName))
                .findFirst()
                .orElseThrow();
    }

    public List<String> getTopics() {
        return orchestraData.getTopics();
    }
}
