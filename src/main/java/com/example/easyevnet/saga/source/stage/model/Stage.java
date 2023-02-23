package com.example.easyevnet.saga.source.stage.model;

import java.time.Duration;
import java.util.function.Consumer;

public record Stage<T>(
        Consumer<String> processor,
        String name,
        String queueName,
        OnError onError,
        Duration timeout) {
}
