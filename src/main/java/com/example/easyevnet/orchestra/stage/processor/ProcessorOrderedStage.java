package com.example.easyevnet.orchestra.stage.processor;

import com.example.easyevnet.broker.kafka.model.ReceivedMessage;
import com.example.easyevnet.orchestra.stage.model.Stage;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProcessorOrderedStage<T> extends StageProcessor {

    private final Stage<?> stageToProcess;

    @Override
    public <ID> Boolean processOrderStage(ReceivedMessage<ID> message) {
        return applyStage(stageToProcess, message.body());
    }

    @Override
    public Boolean isPossibleToPerform(Stage<?> stageBeforeCurrent) {
        return stageBeforeCurrent.equals(stageToProcess);
    }
}
