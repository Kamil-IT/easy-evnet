package com.example.easyevnet.orchestra.stage.processor;

import com.example.easyevnet.broker.kafka.model.ReceivedMessage;
import com.example.easyevnet.orchestra.database.StageStatus;
import com.example.easyevnet.orchestra.stage.model.Stage;
import com.example.easyevnet.orchestra.stage.model.StageOperations;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

abstract class StageProcessor {

    protected static <T> StageStatus applyStage(StageOperations<T> operations, String body, Duration timeout) {
        try {
            CompletableFuture<Boolean> async = CompletableFuture.supplyAsync(() -> {
                operations.processor()
                        .andThen(operations.afterResponseProcess())
                        .accept(body);
                return true;
            });
            async.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            return StageStatus.DONE;

        } catch (ExecutionException | InterruptedException e) {
            operations.onError().accept(e);
            return StageStatus.ERROR;

        } catch (TimeoutException e) {
            operations.onError().accept(e);
            return StageStatus.TIMEOUT;
        }
    }

    public abstract <ID> StageStatus processOrderStage(ReceivedMessage<ID> message);

    public abstract Boolean isPossibleToPerform(Stage<?> stageDataBeforeCurrent);
}
