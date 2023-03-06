package com.example.easyevnet.orchestra;

import com.example.easyevnet.broker.kafka.model.ReceivedMessage;
import com.example.easyevnet.monitor.event.EventPublisher;
import com.example.easyevnet.monitor.event.StageFinishedEvent;
import com.example.easyevnet.monitor.event.WorkflowFinishedWithErrorEvent;
import com.example.easyevnet.orchestra.database.StageStatus;
import com.example.easyevnet.orchestra.orchestra.model.OrchestraData;
import com.example.easyevnet.orchestra.stage.model.Stage;
import com.example.easyevnet.orchestra.stage.processor.ProcessorOrderedStage;
import jakarta.annotation.Nullable;
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

    //    TODO: Not boolean add more complicated object for example message if fail
//    public <E> boolean processNextStep(@Nullable String step, String body) {
//        Stage<?> stageDataToProcess = orchestraData.getAllStages()
//                .stream()
//                .filter(stage -> stage.stageData().isNameEqual(step))
//                .findFirst()
//                .orElseThrow();
//
//
//        StageStatus result = null;
//
////        Może pomyśl o streamch
//        if (orchestraData.getStageData().contains(stageDataToProcess)) {
//            result = new ProcessorStage<>(stageDataToProcess)
//                    .processOrderStage(new ReceivedMessage<>(workflowIdentifier, body, new HashMap<>(), (Stage<String>) stageDataToProcess));
//
//        } else if (orchestraData.getStagesBrakingOrder().contains(stageDataToProcess)) {
//            result = new ProcessorStage<>(stageDataToProcess)
//                    .processOrderStage(new ReceivedMessage<>(workflowIdentifier, body, new HashMap<>(), (Stage<String>) stageDataToProcess));
//
////            currentStageData = stageDataToProcess;
//
//        } else if (isNextOrderedStage(stageDataToProcess)) {
//            result = new ProcessorOrderedStage<>(stageDataToProcess)
//                    .processOrderStage(new ReceivedMessage<>(workflowIdentifier, body, new HashMap<>(), (Stage<String>) stageDataToProcess));
//
////            currentStageData = stageDataToProcess;
//
//        }
//
//        if (result == null) {
//            log.error("Stage not found or stage already done: " + step + " " + stageDataToProcess);
////            throw new IllegalArgumentException("Stage not found or stage already done");
//            return false;
//        }
//
////        if (currentStageData != null && currentStageData == orchestraData.getLastStage()) {
////            publisher.publish(new WorkflowFinishedEvent("Stage completed. Id: " + workflowIdentifier));
////            log.info("Stage completed. Id: " + workflowIdentifier);
////        }
//
//        return false;
//    }


    public <E> StageStatus processNextOrderedStage(@Nullable Stage<E> stage, String body) {

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

//    public boolean isNextOrderedStage(Stage<?> stageDataToProcess) {
//        return orchestraData.getStagesInOrder().contains(stageDataToProcess) &&
//                orchestraData.getOrderedNextStage(currentStageData)
//                        .map(nextStage -> nextStage.equals(stageDataToProcess)).orElse(false);
//    }

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
