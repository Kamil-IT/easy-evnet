package com.example.easyevnet.saga.source.stage.model.builder;

import com.example.easyevnet.saga.source.stage.model.OnError;
import com.example.easyevnet.saga.source.stage.model.Orchestra;
import com.example.easyevnet.saga.source.stage.model.Stage;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;

public class StageBuilder {

    private final Function<Stage<?>, OrchestraBuilder> addStageToOrchestraBuilder;
    private final Consumer<String> processor;
    private final String stageName;
    private final String queueName;

    private OnError onError;
    private Duration timeout;

    StageBuilder(
            Function<Stage<?>, OrchestraBuilder> addStageToOrchestraBuilder,
            Consumer<String> processor,
            String stageName,
            String queueName) {
        this.addStageToOrchestraBuilder = addStageToOrchestraBuilder;
        this.processor = processor;
        this.stageName = stageName;
        this.queueName = queueName;
    }

    public StageBuilder onError(OnError onError) {
        this.onError = onError;
        return this;
    }

    public StageBuilder timeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    public OrchestraBuilder nextStage() {
        return addStageToOrchestraBuilder.apply(new Stage<>(processor, stageName, queueName, onError, timeout));
    }

    public Orchestra build() {
        OrchestraBuilder builder = addStageToOrchestraBuilder.apply(new Stage<>(processor, stageName, queueName, onError, timeout));
        return builder.build();
    }
}
