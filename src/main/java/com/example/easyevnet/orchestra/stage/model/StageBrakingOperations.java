package com.example.easyevnet.orchestra.stage.model;

import java.util.Collection;
import java.util.function.BiConsumer;

public record StageBrakingOperations<T>(
        BiConsumer<T, Collection<Stage<?>>> processor,
        BiConsumer<T, Collection<Stage<?>>> afterResponseProcess,
        BiConsumer<T, Collection<Stage<?>>> afterResponseReceivedConsumer,
        BiConsumer<Exception, Collection<Stage<?>>> onError
) {
}
