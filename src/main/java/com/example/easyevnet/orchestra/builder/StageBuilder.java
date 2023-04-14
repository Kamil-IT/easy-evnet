package com.example.easyevnet.orchestra.builder;

import com.example.easyevnet.orchestra.orchestra.model.OrchestraData;
import com.example.easyevnet.orchestra.orchestra.model.StageType;
import com.example.easyevnet.orchestra.stage.model.Stage;
import com.example.easyevnet.orchestra.stage.model.StageData;
import com.example.easyevnet.orchestra.stage.model.StageOperations;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;

public class StageBuilder<T> extends StageBuilderAbstract<T, Consumer<T>> {

    private Consumer<T> afterResponseProcess;
    private Consumer<T> afterResponseReceivedConsumer;
    private Consumer<Exception> onError;

    StageBuilder(Function<Stage<?>,
            OrchestraBuilder> addStageToOrchestraBuilder,
                 Consumer<T> processor,
                 String stageName,
                 String queueName,
                 StageType stageType,
                 Class<T> bodyClass) {
        super(addStageToOrchestraBuilder, processor, stageName, queueName, stageType, bodyClass);
    }

    public StageBuilder<T> onError(Consumer<Exception> onError) {
        this.onError = onError;
        return this;
    }

    public StageBuilder<T> waitForResponse(Consumer<T> afterResponseReceivedConsumer) {
        this.afterResponseProcess = afterResponseReceivedConsumer;
        return this;
    }

    public StageBuilder<T> afterProcessMessage(Consumer<T> afterProcessMessageConsumer) {
        this.afterResponseReceivedConsumer = afterProcessMessageConsumer;
        return this;
    }

    @Override
    public StageBuilder<T> timeout(Duration timeout) {
        super.timeout(timeout);
        return this;
    }

    public OrchestraBuilder nextStage() {
        var data = new StageData<>(stageName, bodyClass, queueName, timeout, retry);
        var operations = new StageOperations<>(
                processor,
                afterResponseProcess == null ? (i) -> {
                } : afterResponseProcess,
                afterResponseReceivedConsumer == null ? (i) -> {
                } : afterResponseReceivedConsumer,
                onError == null ? (i) -> {
                } : onError);
        return addStageToOrchestraBuilder.apply(new Stage<>(data, operations, stageType));
    }

    public OrchestraData build() {
        return nextStage().build();
    }

    @Override
    public StageBuilder<T> retry(int times) {
        super.retry(times);
        return this;
    }
}
