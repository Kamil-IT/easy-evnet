package com.example.easyevnet.saga.source.stage.model;

import lombok.Getter;

import java.util.Collection;
import java.util.List;

@Getter
public class Orchestra {

    private final List<Stage<?>> stages;
    private final List<Stage<?>> stagesInOrder;
    private final List<Stage<?>> stagesBrakingOrder;

    public Orchestra(Collection<Stage<?>> stages, Collection<Stage<?>> stagesInOrder, Collection<Stage<?>> stagesBrakingOrder) {
        this.stages = List.copyOf(stages);
        this.stagesBrakingOrder = List.copyOf(stagesBrakingOrder);
        this.stagesInOrder = List.copyOf(stagesInOrder);
    }
}
