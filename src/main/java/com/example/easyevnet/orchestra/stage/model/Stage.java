package com.example.easyevnet.orchestra.stage.model;

import com.example.easyevnet.orchestra.orchestra.model.StageType;

public record Stage<T> (StageData<T> stageData, StageOperations<T> stageOperations, StageType stageType){

    public String name() {
        return stageData.name();
    }
}
