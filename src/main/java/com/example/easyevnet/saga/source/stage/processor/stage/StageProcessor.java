package com.example.easyevnet.saga.source.stage.processor.stage;

import com.example.easyevnet.saga.source.stage.model.Stage;

abstract class StageProcessor {

    protected static <T> boolean applyStage(Stage<T> stage, String body) {
        try {
            stage.processor().accept(body);
            return true;
        } catch (Exception e) {
            stage.onError().onError();
            return false;
        }
    }
}
