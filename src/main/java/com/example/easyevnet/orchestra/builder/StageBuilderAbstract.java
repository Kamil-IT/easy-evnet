package com.example.easyevnet.orchestra.builder;

import com.example.easyevnet.orchestra.orchestra.model.StageType;
import com.example.easyevnet.orchestra.stage.model.Stage;

import java.time.Duration;
import java.util.function.Function;

class StageBuilderAbstract<T, PROC> {

    protected final Function<Stage<?>, OrchestraBuilder> addStageToOrchestraBuilder;
    protected final PROC processor;
    protected final String stageName;
    protected final String queueName;
    protected final StageType stageType;
    protected final Class<T> bodyClass;
    protected Duration timeout;
    protected int retry;


    StageBuilderAbstract(
            Function<Stage<?>, OrchestraBuilder> addStageToOrchestraBuilder,
            PROC processor,
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

    protected StageBuilderAbstract<T, PROC> timeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    protected StageBuilderAbstract<T, PROC> retry(int times) {
        this.retry = times;
        return this;
    }
}
