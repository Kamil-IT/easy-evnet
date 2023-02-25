package com.example.easyevnet.orchestra;

import com.example.easyevnet.monitor.EventPublisher;
import com.example.easyevnet.monitor.WorkflowFinishedEvent;
import com.example.easyevnet.broker.kafka.model.ReceivedMessage;
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
    private final ID workflowIdentifier;
    @Getter
    private Stage<?> currentStage = null;

    public StageExecutor(Orchestra orchestra, ID workflowIdentifier) {
        this.orchestra = orchestra;
        this.workflowIdentifier = workflowIdentifier;
        this.publisher = EventPublisher.getInstanceWorkflowFinished();
    }

    public <E> boolean processNextStep(@Nullable String step, String body) {
        Stage<?> stageToProcess = orchestra.getAllStages()
                .stream()
                .filter(stage -> stage.isNameEqual(step))
                .findFirst()
                .orElseThrow();


        Boolean result = null;

//        Może pomyśl o streamch
        if (orchestra.getStages().contains(stageToProcess)) {
            result = new ProcessorStage<>(stageToProcess)
                    .processOrderStage(new ReceivedMessage<>(workflowIdentifier, body, new HashMap<>(), (Stage<String>) stageToProcess));

        } else if (orchestra.getStagesBrakingOrder().contains(stageToProcess)) {
            result = new ProcessorStage<>(stageToProcess)
                    .processOrderStage(new ReceivedMessage<>(workflowIdentifier, body, new HashMap<>(), (Stage<String>) stageToProcess));

            currentStage = stageToProcess;

        } else if (isNextOrderedStage(stageToProcess)) {
            result = new ProcessorOrderedStage<>(stageToProcess)
                    .processOrderStage(new ReceivedMessage<>(workflowIdentifier, body, new HashMap<>(), (Stage<String>) stageToProcess));

            currentStage = stageToProcess;

        }

        if (result == null) {
            log.error("Stage not found or stage already done: " + step + " " + stageToProcess);
//            throw new IllegalArgumentException("Stage not found or stage already done");
            return false;
        }

        if (currentStage != null && currentStage == orchestra.getLastStage()) {
            publisher.publish(new WorkflowFinishedEvent("Stage completed. Id: " + workflowIdentifier));
            log.info("Stage completed. Id: " + workflowIdentifier);
        }

        return result;
    }

    private boolean isNextOrderedStage(Stage<?> stageToProcess) {
        return orchestra.getStagesInOrder().contains(stageToProcess) &&
                orchestra.getOrderedNextStage(currentStage).map(nextStage -> nextStage.equals(stageToProcess)).orElse(false);
    }

    public List<String> getTopics() {
        return orchestra.getTopics();
    }
}
