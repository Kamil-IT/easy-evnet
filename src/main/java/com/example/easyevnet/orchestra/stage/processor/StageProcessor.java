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

    protected static <T> StageStatus applyStage(StageOperations<T> operations, String body, Duration timeout, int retryTimes) {

        StageStatus status = StageStatus.ERROR;

        int retry = 0;
        for (; retry <= retryTimes; retry++) {
            try {
                status = processStage(operations, body, timeout);
            } catch (TimeoutException e) {
                status = StageStatus.TIMEOUT;
                if (retry == retryTimes) {
                    operations.onError().accept(e);
                }
            }
            
            if (!status.equals(StageStatus.TIMEOUT)) {
                return status;
            }
        }

        if (retry > 0) {
            operations.onError().accept(new TimeoutException());
        }

        return status;
    }

    private static <T> StageStatus processStage(StageOperations<T> operations, String body, Duration timeout) throws TimeoutException {
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

        }
    }

    public abstract <ID> StageStatus processOrderStage(ReceivedMessage<ID> message);

    public abstract Boolean isPossibleToPerform(Stage<?> stageDataBeforeCurrent);
}
