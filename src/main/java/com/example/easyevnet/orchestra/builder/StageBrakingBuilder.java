package com.example.easyevnet.orchestra.builder;

import com.example.easyevnet.orchestra.orchestra.model.OrchestraData;
import com.example.easyevnet.orchestra.orchestra.model.StageType;
import com.example.easyevnet.orchestra.stage.model.Stage;
import com.example.easyevnet.orchestra.stage.model.StageBrakingOperations;
import com.example.easyevnet.orchestra.stage.model.StageData;
import org.apache.commons.lang3.NotImplementedException;

import java.time.Duration;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class StageBrakingBuilder<T> extends StageBuilderAbstract<T, BiConsumer<T, Collection<Stage<?>>>> {

    private BiConsumer<T, Collection<Stage<?>>> afterResponseProcess;
    private BiConsumer<T, Collection<Stage<?>>> afterResponseReceivedConsumer;
    private BiConsumer<Exception, Collection<Stage<?>>> onError;

    public StageBrakingBuilder(Function<Stage<?>,
            OrchestraBuilder> addStageToOrchestraBuilder,
                               BiConsumer<T, Collection<Stage<?>>> processor,
                               String stageName,
                               String queueName,
                               StageType stageType,
                               Class<T> bodyClass) {
        super(addStageToOrchestraBuilder, processor, stageName, queueName, stageType, bodyClass);
    }

    public StageBrakingBuilder<T> onError(BiConsumer<Exception, Collection<Stage<?>>> onError) {
        this.onError = onError;
        return this;
    }

    public StageBrakingBuilder<T> waitForResponse(BiConsumer<T, Collection<Stage<?>>> afterResponseReceivedConsumer) {
        this.afterResponseProcess = afterResponseReceivedConsumer;
        return this;
    }

    public StageBrakingBuilder<T> afterProcessMessage(BiConsumer<T, Collection<Stage<?>>> afterProcessMessageConsumer) {
        this.afterResponseReceivedConsumer = afterProcessMessageConsumer;
        return this;
    }

    @Override
    public StageBrakingBuilder<T> timeout(Duration timeout) {
        super.timeout(timeout);
        return this;
    }

    public OrchestraBuilder nextStage() {
        var data = new StageData<>(stageName, bodyClass, queueName, timeout, retry);
        var operations = new StageBrakingOperations<>(
                processor,
                afterResponseProcess == null ? (i, i2) -> {
                } : afterResponseProcess,
                afterResponseReceivedConsumer == null ? (i, i2) -> {
                } : afterResponseReceivedConsumer,
                onError == null ? (i, i2) -> {
                } : onError);
//        How to handle it? Add new collection in stage :)
//        return addStageToOrchestraBuilder.apply(new Stage<>(data, operations, stageType));
        throw new NotImplementedException("How to handle it?");
    }

    public OrchestraData build() {
        return nextStage().build();
    }

    @Override
    public StageBrakingBuilder<T> retry(int times) {
        super.retry(times);
        return this;
    }
}
