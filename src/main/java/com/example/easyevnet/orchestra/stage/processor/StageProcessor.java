package com.example.easyevnet.orchestra.stage.processor;

import com.example.easyevnet.broker.kafka.model.ReceivedMessage;
import com.example.easyevnet.monitor.api.model.ResponseList;
import com.example.easyevnet.orchestra.orchestra.model.StageStatus;
import com.example.easyevnet.orchestra.stage.model.Stage;
import com.example.easyevnet.orchestra.stage.model.StageOperations;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RequiredArgsConstructor
abstract class StageProcessor<ID, T>  {

    protected StageStatus applyStage(StageOperations<T> operations, T body, Duration timeout, int retryTimes) {
        StageStatus status = StageStatus.ERROR;

//        TODO: Use fancy library instead of for
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

    private static <T> StageStatus processStage(StageOperations<T> operations, T body, Duration timeout) throws TimeoutException {
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

    public abstract StageStatus processOrderStage(ReceivedMessage<ID, T> message);

}
