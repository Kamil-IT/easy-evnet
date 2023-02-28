package com.example.easyevnet.orchestra.stage.processor;

import com.example.easyevnet.broker.kafka.model.ReceivedMessage;
import com.example.easyevnet.orchestra.stage.model.Stage;
import com.example.easyevnet.orchestra.stage.model.StageData;
import com.example.easyevnet.orchestra.stage.model.StageOperations;

abstract class StageProcessor {

    protected static <T> boolean applyStage(StageOperations<T> operations, String body) {
        try {
            operations.processor()
                    .andThen(operations.afterResponseProcess())
                    .accept(body);
            return true;
        } catch (Exception e) {
            operations.onError().accept(e);
            return false;
        }
    }

    public abstract <ID> Boolean processOrderStage(ReceivedMessage<ID> message);

    public abstract Boolean isPossibleToPerform(Stage<?> stageDataBeforeCurrent);
}
