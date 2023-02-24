package com.example.easyevnet.orchestra.orchestra.model;

import com.example.easyevnet.orchestra.stage.model.Stage;
import lombok.Getter;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public Optional<Stage<?>> getOrderedNextStage(Stage<?> stage) {
        return Optional.of(stagesInOrder.indexOf(stage))
                .filter(index -> index != -1)
                .map(index -> index + 1)
                .filter(index -> index < stagesInOrder.size())
                .map(stagesInOrder::get);
    }

    public List<Stage<?>> getNextStages(Stage<?> stage) {
        return Stream.of(getOrderedNextStage(stage).stream(), stagesBrakingOrder.stream(), stages.stream())
                .flatMap(i -> i)
                .collect(Collectors.toList());
    }
}
