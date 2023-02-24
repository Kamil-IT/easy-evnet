package com.example.easyevnet.orchestra.stage.processor;

import com.example.easyevnet.broker.kafka.model.ReceivedMessage;
import com.example.easyevnet.orchestra.stage.model.Stage;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProcessorStage<T> extends StageProcessor {

    private final Stage<T> stageToProcess;

    public <ID> Boolean processOrderStage(ReceivedMessage<ID> message) {
        return applyStage(stageToProcess, message.body());
    }

    public Boolean isPossibleToPerform(Stage<?> stageBeforeCurrent) {
        return stageBeforeCurrent.equals(stageToProcess);
    }
}
