package com.example.easyevnet.orchestra.builder;

import com.example.easyevnet.orchestra.orchestra.model.StageType;
import com.example.easyevnet.orchestra.orchestra.model.OrchestraData;
import com.example.easyevnet.orchestra.stage.model.Stage;
import com.example.easyevnet.orchestra.stage.model.StageData;
import com.example.easyevnet.orchestra.stage.model.StageOperations;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;

public class StageBuilder {

    private final Function<Stage<?>, OrchestraBuilder> addStageToOrchestraBuilder;
    private final Consumer<String> processor;
    private final String stageName;
    private final String queueName;
    private final StageType stageType;

    private Consumer<String> afterResponseProcess;
    private Consumer<String> afterResponseReceivedConsumer;

    private Consumer<Exception> onError;
    private Duration timeout;
    private int retry;

    StageBuilder(
            Function<Stage<?>, OrchestraBuilder> addStageToOrchestraBuilder,
            Consumer<String> processor,
            String stageName,
            String queueName,
            StageType stageType) {
        this.addStageToOrchestraBuilder = addStageToOrchestraBuilder;
        this.processor = processor;
        this.stageName = stageName;
        this.queueName = queueName;
        this.stageType = stageType;
    }

    public StageBuilder onError(Consumer<Exception> onError) {
        this.onError = onError;
        return this;
    }

    public StageBuilder timeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    public OrchestraBuilder nextStage() {
        var data = new StageData<>(stageName, queueName, timeout, retry);
        var operations = new StageOperations(
                processor,
                afterResponseProcess == null ? (i) -> {} : afterResponseProcess,
                afterResponseReceivedConsumer == null ? (i) -> {} : afterResponseReceivedConsumer,
                onError == null ? (i) -> {} : onError);
        return addStageToOrchestraBuilder.apply(new Stage<>(data, operations, stageType));
    }

    public OrchestraData build() {
        var data = new StageData<>(stageName, queueName, timeout, retry);
        var operations = new StageOperations(
                processor,
                afterResponseProcess == null ? (i) -> {} : afterResponseProcess,
                afterResponseReceivedConsumer == null ? (i) -> {} : afterResponseReceivedConsumer,
                onError == null ? (i) -> {} : onError);

        OrchestraBuilder builder = addStageToOrchestraBuilder.apply(new Stage<>(data, operations, stageType));
        return builder.build();
    }

    public StageBuilder waitForResponse(Consumer<String> afterResponseReceivedConsumer) {
        this.afterResponseProcess = afterResponseReceivedConsumer;
        return this;
    }

    public StageBuilder afterProcessMessage(Consumer<String> afterProcessMessageConsumer) {
        this.afterResponseReceivedConsumer = afterProcessMessageConsumer;
        return this;
    }

    public StageBuilder retry(int times) {
        this.retry = times;
        return this;
    }
}
