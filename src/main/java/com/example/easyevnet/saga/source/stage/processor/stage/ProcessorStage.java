package com.example.easyevnet.saga.source.stage.processor.stage;

import com.example.easyevnet.saga.source.stage.StateMessage;
import com.example.easyevnet.saga.source.stage.model.Stage;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProcessorStage<T> extends StageProcessor {

    private final Stage<T> stageToProcess;

    public <ID> Boolean processOrderStage(StateMessage<ID> message) {
        return applyStage(stageToProcess, message.body());
    }

    public Boolean isPossibleToPerform(Stage<?> stageBeforeCurrent) {
        return stageBeforeCurrent.equals(stageToProcess);
    }
}
