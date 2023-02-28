package com.example.easyevnet.orchestra.builder;

import com.example.easyevnet.orchestra.orchestra.model.Orchestra;
import com.example.easyevnet.orchestra.stage.model.Stage;
import com.example.easyevnet.orchestra.stage.model.StageData;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class OrchestraBuilder {

    private final List<Stage<?>> stageData = new ArrayList<>();
    private final List<Stage<?>> stagesInOrder = new ArrayList<>();
    private final List<Stage<?>> stagesBrakingOrder = new ArrayList<>();

    public StageBuilder addStage(Consumer<String> processor, String name) {
        return new StageBuilder(this::addStage, processor, name, "");
    }

    public <T> StageBuilder stageInOrder(Consumer<String> processor, Enum<?> stageName) {
        String queueName = stageName.getClass().getName() + "." + stageName.name();
        return new StageBuilder(this::stageInOrder, processor, stageName.name(), queueName);
    }

//    To musi być bardzeij rozbudowane co oznacza ze bedzie bardziej złożony obiekt wejsciowy do fukcji processor
//    a moze 2 argumenty? pierwszy message a drugi to bedzie detail o zrobionych sagach
    public StageBuilder addStagesBrakingOrder(Consumer<String> processor, String name) {
        return new StageBuilder(this::addStagesBrakingOrder, processor, name, "");
    }

    OrchestraBuilder addStage(Stage<?> stageData) {
        this.stageData.add(stageData);
        return this;
    }

    OrchestraBuilder stageInOrder(Stage<?> stageData) {
        this.stagesInOrder.add(stageData);
        return this;
    }

    OrchestraBuilder addStagesBrakingOrder(Stage<?> stageData) {
        this.stagesBrakingOrder.add(stageData);
        return this;
    }

    public Orchestra build() {
        return new Orchestra(stageData, stagesInOrder, stagesBrakingOrder);
    }

}