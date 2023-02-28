package com.example.easyevnet.orchestra;

import com.example.easyevnet.broker.kafka.model.ReceivedMessage;
import com.example.easyevnet.monitor.event.EventPublisher;
import com.example.easyevnet.monitor.event.WorkflowFinishedEvent;
import com.example.easyevnet.orchestra.orchestra.model.Orchestra;
import com.example.easyevnet.orchestra.stage.model.Stage;
import com.example.easyevnet.orchestra.stage.processor.ProcessorOrderedStage;
import com.example.easyevnet.orchestra.stage.processor.ProcessorStage;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;

@Slf4j
public class StageExecutor<ID> {

    private final EventPublisher<WorkflowFinishedEvent> publisher;

    private final Orchestra orchestra;
    @Getter
    private final ID workflowIdentifier;
    @Getter
//    TODO: Should be get from db
    private Stage<?> currentStageData = null;

    public StageExecutor(Orchestra orchestra, ID workflowIdentifier) {
        this.orchestra = orchestra;
        this.workflowIdentifier = workflowIdentifier;
        this.publisher = EventPublisher.getInstanceWorkflowFinished();
    }

//    TODO: Not boolean add more complicated object for example message if fail
    public <E> boolean processNextStep(@Nullable String step, String body) {
        Stage<?> stageDataToProcess = orchestra.getAllStages()
                .stream()
                .filter(stage -> stage.stageData().isNameEqual(step))
                .findFirst()
                .orElseThrow();


        Boolean result = null;

//        Może pomyśl o streamch
        if (orchestra.getStageData().contains(stageDataToProcess)) {
            result = new ProcessorStage<>(stageDataToProcess)
                    .processOrderStage(new ReceivedMessage<>(workflowIdentifier, body, new HashMap<>(), (Stage<String>) stageDataToProcess));

        } else if (orchestra.getStagesBrakingOrder().contains(stageDataToProcess)) {
            result = new ProcessorStage<>(stageDataToProcess)
                    .processOrderStage(new ReceivedMessage<>(workflowIdentifier, body, new HashMap<>(), (Stage<String>) stageDataToProcess));

            currentStageData = stageDataToProcess;

        } else if (isNextOrderedStage(stageDataToProcess)) {
            result = new ProcessorOrderedStage<>(stageDataToProcess)
                    .processOrderStage(new ReceivedMessage<>(workflowIdentifier, body, new HashMap<>(), (Stage<String>) stageDataToProcess));

            currentStageData = stageDataToProcess;

        }

        if (result == null) {
            log.error("Stage not found or stage already done: " + step + " " + stageDataToProcess);
//            throw new IllegalArgumentException("Stage not found or stage already done");
            return false;
        }

        if (currentStageData != null && currentStageData == orchestra.getLastStage()) {
            publisher.publish(new WorkflowFinishedEvent("Stage completed. Id: " + workflowIdentifier));
            log.info("Stage completed. Id: " + workflowIdentifier);
        }

        return result;
    }

    private boolean isNextOrderedStage(Stage<?> stageDataToProcess) {
        return orchestra.getStagesInOrder().contains(stageDataToProcess) &&
                orchestra.getOrderedNextStage(currentStageData)
                        .map(nextStage -> nextStage.equals(stageDataToProcess)).orElse(false);
    }

    public List<String> getTopics() {
        return orchestra.getTopics();
    }
}
