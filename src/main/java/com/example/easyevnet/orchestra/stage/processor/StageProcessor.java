package com.example.easyevnet.orchestra.stage.processor;

import com.example.easyevnet.broker.kafka.model.ReceivedMessage;
import com.example.easyevnet.orchestra.database.StageStatus;
import com.example.easyevnet.orchestra.stage.model.Stage;
import com.example.easyevnet.orchestra.stage.model.StageOperations;

abstract class StageProcessor {

    protected static <T> StageStatus applyStage(StageOperations<T> operations, String body) {
        try {
            operations.processor()
                    .andThen(operations.afterResponseProcess())
                    .accept(body);
            return StageStatus.DONE;
        } catch (Exception e) {
            operations.onError().accept(e);
            return StageStatus.ERROR;
        }
    }

    public abstract <ID> StageStatus processOrderStage(ReceivedMessage<ID> message);

    public abstract Boolean isPossibleToPerform(Stage<?> stageDataBeforeCurrent);
}
