package com.example.easyevnet.orchestra.stage.processor;

import com.example.easyevnet.broker.kafka.model.ReceivedMessage;
import com.example.easyevnet.orchestra.stage.model.Stage;

abstract class StageProcessor {

    protected static <T> boolean applyStage(Stage<T> stage, String body) {
        try {
            stage.processor().accept(body);
            return true;
        } catch (Exception e) {
            stage.onError().accept(e);
            return false;
        }
    }

    public abstract <ID> Boolean processOrderStage(ReceivedMessage<ID> message);

    public abstract Boolean isPossibleToPerform(Stage<?> stageBeforeCurrent);
}
