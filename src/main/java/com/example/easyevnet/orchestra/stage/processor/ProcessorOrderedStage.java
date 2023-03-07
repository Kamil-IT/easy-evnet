package com.example.easyevnet.orchestra.stage.processor;

import com.example.easyevnet.broker.kafka.model.ReceivedMessage;
import com.example.easyevnet.orchestra.database.StageStatus;
import com.example.easyevnet.orchestra.stage.model.Stage;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProcessorOrderedStage<T> extends StageProcessor {

    private final Stage<?> stageDataToProcess;

    @Override
    public <ID> StageStatus processOrderStage(ReceivedMessage<ID> message) {
        return applyStage(stageDataToProcess.stageOperations(), message.body(), stageDataToProcess.stageData().timeout());
    }

    @Override
    public Boolean isPossibleToPerform(Stage<?> stageDataBeforeCurrent) {
        return stageDataBeforeCurrent.equals(stageDataToProcess);
    }
}
