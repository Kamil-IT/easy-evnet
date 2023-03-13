package com.example.easyevnet.orchestra.stage;

import com.example.easyevnet.broker.kafka.model.ReceivedMessage;
import com.example.easyevnet.orchestra.orchestra.model.OrchestraData;
import com.example.easyevnet.orchestra.orchestra.model.StageStatus;
import com.example.easyevnet.orchestra.stage.model.Stage;
import com.example.easyevnet.orchestra.stage.processor.ProcessorOrderedStage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;

@Slf4j
public class StageExecutor<ID> {

    @Getter
    private final OrchestraData orchestraData;
    @Getter
    private final ID workflowIdentifier;

    public StageExecutor(OrchestraData orchestraData, ID workflowIdentifier) {
        this.orchestraData = orchestraData;
        this.workflowIdentifier = workflowIdentifier;
    }

    //    TODO: Not StageStatus add more complicated object for example message if fail
    public <E> StageStatus processNextOrderedStage(Stage<E> stage, E body) {

        StageStatus status = new ProcessorOrderedStage<>(stage)
                .processOrderStage(new ReceivedMessage<>(workflowIdentifier, body, new HashMap<>(), stage));

        return status;
    }


    public <E> StageStatus processNextDefaultStage(Stage<E> stage, E body) {

//        TODO: Implement
        StageStatus status = new ProcessorOrderedStage<>(stage)
                .processOrderStage(new ReceivedMessage<>(workflowIdentifier, body, new HashMap<>(), stage));

        return status;
    }

    public <E> StageStatus processNextBrakingStage(Stage<E> stage, E body) {
        //        TODO: Implement

        StageStatus status = new ProcessorOrderedStage<>(stage)
                .processOrderStage(new ReceivedMessage<>(workflowIdentifier, body, new HashMap<>(), stage));

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
