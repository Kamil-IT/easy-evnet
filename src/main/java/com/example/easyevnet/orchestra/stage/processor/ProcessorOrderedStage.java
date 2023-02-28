package com.example.easyevnet.orchestra.stage.processor;

import com.example.easyevnet.broker.kafka.model.ReceivedMessage;
import com.example.easyevnet.orchestra.stage.model.Stage;
import com.example.easyevnet.orchestra.stage.model.StageData;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProcessorOrderedStage<T> extends StageProcessor {

    private final Stage<?> stageDataToProcess;

    @Override
    public <ID> Boolean processOrderStage(ReceivedMessage<ID> message) {
        return applyStage(stageDataToProcess.stageOperations(), message.body());
    }

    @Override
    public Boolean isPossibleToPerform(Stage<?> stageDataBeforeCurrent) {
        return stageDataBeforeCurrent.equals(stageDataToProcess);
    }
}
