package com.example.easyevnet.saga.source.stage.model.builder;

import com.example.easyevnet.saga.source.stage.model.Orchestra;
import com.example.easyevnet.saga.source.stage.model.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class OrchestraBuilder {

    private final List<Stage<?>> stages = new ArrayList<>();
    private final List<Stage<?>> stagesInOrder = new ArrayList<>();
    private final List<Stage<?>> stagesBrakingOrder = new ArrayList<>();

    public StageBuilder addStage(Consumer<String> processor, String name) {
        return new StageBuilder(this::addStage, processor, name, "");
    }

    public <T> StageBuilder addStagesInOrder(Consumer<String> processor, Enum<?> stageName) {
        String queueName = stageName.getClass() + "." + stageName.name();
        return new StageBuilder(this::addStagesInOrder, processor, stageName.name(), queueName);
    }

    public StageBuilder addStagesBrakingOrder(Consumer<String> processor, String name) {
        return new StageBuilder(this::addStagesBrakingOrder, processor, name, "");
    }

    OrchestraBuilder addStage(Stage<?> stage) {
        this.stages.add(stage);
        return this;
    }

    OrchestraBuilder addStagesInOrder(Stage<?> stage) {
        this.stagesInOrder.add(stage);
        return this;
    }

    OrchestraBuilder addStagesBrakingOrder(Stage<?> stage) {
        this.stagesBrakingOrder.add(stage);
        return this;
    }

    public Orchestra build() {
        return new Orchestra(stages, stagesInOrder, stagesBrakingOrder);
    }

}
