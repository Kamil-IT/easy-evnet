package com.example.easyevnet.orchestra.stage.processor;

import com.example.easyevnet.broker.kafka.model.ReceivedMessage;
import com.example.easyevnet.orchestra.orchestra.model.StageStatus;
import com.example.easyevnet.orchestra.stage.model.Stage;

public class ProcessorOrderedStage<ID, T> extends StageProcessor<ID, T> {

    private final Stage<T> stage;

    public ProcessorOrderedStage(Stage<T> stageDataToProcess) {
        this.stage = stageDataToProcess;
    }

    @Override
    public StageStatus processOrderStage(ReceivedMessage<ID, T> message) {
        return applyStage(stage.stageOperations(), message.body(), stage.stageData().timeout(), stage.stageData().retry());
    }
}
