package com.example.easyevnet.orchestra.builder;

import com.example.easyevnet.orchestra.orchestra.model.OrchestraData;
import com.example.easyevnet.orchestra.orchestra.model.StageType;
import com.example.easyevnet.orchestra.stage.model.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class OrchestraBuilder {

    private final List<Stage<?>> stageData = new ArrayList<>();
    private final List<Stage<?>> stagesInOrder = new ArrayList<>();
    private final List<Stage<?>> stagesBrakingOrder = new ArrayList<>();

//    TODO:
//    public <T> StageBuilder<T> lastStage

    private static String getQueueName(Enum<?> stageName) {
        return stageName.getClass().getName() + "." + stageName.name();
    }

    public <T> StageBuilder<T> stage(Consumer<T> processor, Enum<?> stageName, Class<T> bodyClass) {
        String queueName = getQueueName(stageName);
        return new StageBuilder<>(this::stage, processor, stageName.name(), queueName, StageType.DEFAULT, bodyClass);
    }

    public <T> StageBuilder<T> stageInOrder(Consumer<T> processor, Enum<?> stageName, Class<T> bodyClass) {
        String queueName = getQueueName(stageName);
        return new StageBuilder<>(this::stageInOrder, processor, stageName.name(), queueName, StageType.ORDERED, bodyClass);
    }

//    To musi być bardzeij rozbudowane co oznacza ze bedzie bardziej złożony obiekt wejsciowy do fukcji processor
//    a moze 2 argumenty? pierwszy message a drugi to bedzie detail o zrobionych sagach
public <T> StageBuilder<T> stagesBrakingOrder(Consumer<T> processor, Enum<?> stageName, Class<T> bodyClass) {
    String queueName = getQueueName(stageName);
    return new StageBuilder<>(this::stagesBrakingOrder, processor, stageName.name(), queueName, StageType.DEFAULT, bodyClass);
}

    OrchestraBuilder stage(Stage<?> stageData) {
        this.stageData.add(stageData);
        return this;
    }

    OrchestraBuilder stageInOrder(Stage<?> stageData) {
        this.stagesInOrder.add(stageData);
        return this;
    }

    OrchestraBuilder stagesBrakingOrder(Stage<?> stageData) {
        this.stagesBrakingOrder.add(stageData);
        return this;
    }

    public OrchestraData build() {
        return new OrchestraData(stageData, stagesInOrder, stagesBrakingOrder);
    }

}
