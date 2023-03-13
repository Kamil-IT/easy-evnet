package com.example.easyevnet.orchestra.builder;

import com.example.easyevnet.orchestra.orchestra.model.StageType;
import com.example.easyevnet.orchestra.orchestra.model.OrchestraData;
import com.example.easyevnet.orchestra.stage.model.Stage;
import com.example.easyevnet.orchestra.stage.model.StageData;
import com.example.easyevnet.orchestra.stage.model.StageOperations;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;

public class StageBuilder<T> {

    private final Function<Stage<?>, OrchestraBuilder> addStageToOrchestraBuilder;
    private final Consumer<T> processor;
    private final String stageName;
    private final String queueName;
    private final StageType stageType;
    private final Class<T> bodyClass;

    private Consumer<T> afterResponseProcess;
    private Consumer<T> afterResponseReceivedConsumer;

    private Consumer<Exception> onError;
    private Duration timeout;
    private int retry;

    StageBuilder(
            Function<Stage<?>, OrchestraBuilder> addStageToOrchestraBuilder,
            Consumer<T> processor,
            String stageName,
            String queueName,
            StageType stageType,
            Class<T> bodyClass) {
        this.addStageToOrchestraBuilder = addStageToOrchestraBuilder;
        this.processor = processor;
        this.stageName = stageName;
        this.queueName = queueName;
        this.stageType = stageType;
        this.bodyClass = bodyClass;
    }

    public StageBuilder<T> onError(Consumer<Exception> onError) {
        this.onError = onError;
        return this;
    }

    public StageBuilder<T> timeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    public OrchestraBuilder nextStage() {
        var data = new StageData<>(stageName, bodyClass, queueName, timeout, retry);
        var operations = new StageOperations<>(
                processor,
                afterResponseProcess == null ? (i) -> {} : afterResponseProcess,
                afterResponseReceivedConsumer == null ? (i) -> {} : afterResponseReceivedConsumer,
                onError == null ? (i) -> {} : onError);
        return addStageToOrchestraBuilder.apply(new Stage<>(data, operations, stageType));
    }

    public OrchestraData build() {
        return nextStage().build();
    }

    public StageBuilder<T> waitForResponse(Consumer<T> afterResponseReceivedConsumer) {
        this.afterResponseProcess = afterResponseReceivedConsumer;
        return this;
    }

    public StageBuilder<T> afterProcessMessage(Consumer<T> afterProcessMessageConsumer) {
        this.afterResponseReceivedConsumer = afterProcessMessageConsumer;
        return this;
    }

    public StageBuilder<T> retry(int times) {
        this.retry = times;
        return this;
    }
}
