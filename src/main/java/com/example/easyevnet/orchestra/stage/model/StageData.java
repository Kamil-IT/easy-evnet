package com.example.easyevnet.orchestra.stage.model;

import com.fasterxml.jackson.core.type.TypeReference;

import java.time.Duration;
import java.util.function.Consumer;

public record StageData<T>(
        String name,
        Class<T> bodyClass,
        String queueName,
        Duration timeout,
        int retry) {

    public boolean isNameEqual(String name) {
        return name != null && name.equals(this.name);
    }
    public boolean isNameEqual(StageData<?> stageData) {
        return stageData != null && isNameEqual(stageData.name);
    }
}
