package com.example.easyevnet.orchestra.orchestra.builder;

import com.example.easyevnet.orchestra.orchestra.model.Orchestra;
import com.example.easyevnet.orchestra.stage.model.Stage;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;

public class StageBuilder {

    private final Function<Stage<?>, OrchestraBuilder> addStageToOrchestraBuilder;
    private final Consumer<String> processor;
    private final String stageName;
    private final String queueName;

    private Consumer<Exception> onError;
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

    public StageBuilder onError(Consumer<Exception> onError) {
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
