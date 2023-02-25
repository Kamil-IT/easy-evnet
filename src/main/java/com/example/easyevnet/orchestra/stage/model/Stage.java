package com.example.easyevnet.orchestra.stage.model;

import java.time.Duration;
import java.util.function.Consumer;

public record Stage<T>(
        Consumer<String> processor,
        String name,
        String queueName,
        Consumer<Exception> onError,
        Duration timeout) {

    public boolean isNameEqual(String name) {
        return name != null && name.equals(this.name);
    }
    public boolean isNameEqual(Stage<?> stage) {
        return stage != null && isNameEqual(stage.name);
    }
}
