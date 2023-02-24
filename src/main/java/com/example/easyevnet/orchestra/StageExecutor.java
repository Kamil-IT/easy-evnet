package com.example.easyevnet.orchestra;

import com.example.easyevnet.orchestra.orchestra.model.Orchestra;
import com.example.easyevnet.orchestra.stage.model.Stage;
import com.example.easyevnet.broker.kafka.model.ReceivedMessage;
import com.example.easyevnet.orchestra.stage.processor.ProcessorOrderedStage;
import com.example.easyevnet.orchestra.stage.processor.ProcessorStage;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class StageExecutor<ID> {

    private final Orchestra orchestra;
    private final ID workflowIdentifier;
    @Getter
    private Stage<?> currentStage;

    public StageExecutor(Orchestra orchestra, ID workflowIdentifier) {
        this.orchestra = orchestra;
        this.workflowIdentifier = workflowIdentifier;

        currentStage = orchestra.getStagesInOrder().get(0);
    }

    public <E> boolean processNextStep(@Nullable String step, String body) {
        Stage<?> stageToProcess = orchestra.getStagesInOrder()
                .stream()
                .filter(stage -> stage.name().equals(step))
                .findFirst()
                .orElseThrow();

        List<Stage<?>> stages = orchestra.getStages();
        List<Stage<?>> stagesBrakingOrder = orchestra.getStagesBrakingOrder();
        List<Stage<?>> stagesOrder = orchestra.getStagesInOrder();


        Boolean result = null;

//        Może pomyśl o streamch
        if (stages.contains(stageToProcess)) {
            result = new ProcessorStage<>(stageToProcess)
                    .processOrderStage(new ReceivedMessage<>(workflowIdentifier, body, new HashMap<>(), (Stage<String>) stageToProcess));

        } else if (stagesBrakingOrder.contains(stageToProcess)) {
            result = new ProcessorStage<>(stageToProcess)
                    .processOrderStage(new ReceivedMessage<>(workflowIdentifier, body, new HashMap<>(), (Stage<String>) stageToProcess));

        } else if (currentStage == null || (stagesOrder.contains(stageToProcess) && new ProcessorOrderedStage<>(stageToProcess).isPossibleToPerform(getNextOrderedStage()))) {
            result = new ProcessorOrderedStage<>(stageToProcess)
                    .processOrderStage(new ReceivedMessage<>(workflowIdentifier, body, new HashMap<>(), (Stage<String>) stageToProcess));
        }

        if (result == null) {
            log.error("Stage not found or stage already done");
//            throw new IllegalArgumentException("Stage not found or stage already done");
        }

        currentStage = stageToProcess;
        return result != null && result;
    }

    public boolean isLastStageDone() {
        int lastStageIndex = orchestra.getStagesInOrder().size() - 1;
        return orchestra.getStagesInOrder().get(lastStageIndex).equals(currentStage);
    }

    public List<String> getTopics() {
        return Stream.of(orchestra.getStagesInOrder().stream(),
                        orchestra.getStages().stream(),
                        orchestra.getStagesBrakingOrder().stream())
                .flatMap(i -> i)
                .map(Stage::queueName)
                .collect(Collectors.toList());
    }

    public Stage<?> getNextOrderedStage() {
        return orchestra.getOrderedNextStage(currentStage)
                .orElseThrow();
    }
}
