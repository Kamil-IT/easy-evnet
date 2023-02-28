package com.example.easyevnet.orchestra.orchestra.model;

import com.example.easyevnet.orchestra.stage.model.Stage;
import com.example.easyevnet.orchestra.stage.model.StageData;
import jakarta.annotation.Nullable;
import lombok.Getter;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class Orchestra {

    private final List<Stage<?>> stageData;
    private final List<Stage<?>> stagesInOrder;
    private final List<Stage<?>> stagesBrakingOrder;

    public Orchestra(Collection<Stage<?>> stageData, Collection<Stage<?>> stagesInOrder, Collection<Stage<?>> stagesBrakingOrder) {
        this.stageData = List.copyOf(stageData);
        this.stagesBrakingOrder = List.copyOf(stagesBrakingOrder);
        this.stagesInOrder = List.copyOf(stagesInOrder);
    }

    public Optional<Stage<?>> getOrderedNextStage(@Nullable Stage<?> stageData) {
        if (stageData == null) {
            return getFirstStage();
        }
        return Optional.of(stagesInOrder.indexOf(stageData))
                .filter(index -> index != -1)
                .map(index -> index + 1)
                .filter(index -> index < stagesInOrder.size())
                .map(stagesInOrder::get);
    }

    private Optional<Stage<?>> getFirstStage() {
        return stagesInOrder.isEmpty() ? Optional.empty() : Optional.of(stagesInOrder.get(0));
    }

    public List<Stage<?>> getNextStages(Stage<?> stageData) {
        return Stream.of(getOrderedNextStage(stageData).stream(), stagesBrakingOrder.stream(), this.stageData.stream())
                .flatMap(i -> i)
                .collect(Collectors.toList());
    }

    public List<Stage<?>> getAllStages() {
        return Stream.of(getStagesInOrder(), getStageData(), getStagesBrakingOrder())
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public List<String> getTopics() {
        return Stream.of(getStagesInOrder(), getStageData(), getStagesBrakingOrder())
                .flatMap(List::stream)
                .map(Stage::stageData)
                .map(StageData::queueName)
                .collect(Collectors.toList());
    }

    @Nullable
    public Stage<?> getLastStage() {
        int size = getStagesInOrder().size();
        return size == 0 ? null : getStagesInOrder().get(size - 1);
    }
}
