package com.example.easyevnet.orchestra.stage.processor;

import com.example.easyevnet.broker.kafka.model.ReceivedMessage;
import com.example.easyevnet.orchestra.orchestra.model.StageStatus;
import com.example.easyevnet.orchestra.stage.model.Stage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Type;

public class ProcessorStage<ID, T> extends StageProcessor<ID, T> {

    private final Stage<T> stageDataToProcess;

    public ProcessorStage(ObjectMapper objectMapper, Stage<T> stageDataToProcess) {
        this.stageDataToProcess = stageDataToProcess;
    }

    public StageStatus processOrderStage(ReceivedMessage<ID, T> message) {
        return applyStage(stageDataToProcess.stageOperations(), message.body(), stageDataToProcess.stageData().timeout(), stageDataToProcess.stageData().retry());
    }
}
