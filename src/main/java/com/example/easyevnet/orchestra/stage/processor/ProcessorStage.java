package com.example.easyevnet.orchestra.stage.processor;

import com.example.easyevnet.broker.kafka.model.ReceivedMessage;
import com.example.easyevnet.orchestra.stage.model.Stage;
import com.example.easyevnet.orchestra.stage.model.StageData;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProcessorStage<T> extends StageProcessor {

    private final Stage<T> stageDataToProcess;

    public <ID> Boolean processOrderStage(ReceivedMessage<ID> message) {
        return applyStage(stageDataToProcess.stageOperations(), message.body());
    }

    public Boolean isPossibleToPerform(Stage<?> stageDataBeforeCurrent) {
        return stageDataBeforeCurrent.equals(stageDataToProcess);
    }
}
