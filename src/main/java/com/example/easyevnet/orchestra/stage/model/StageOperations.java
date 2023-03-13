package com.example.easyevnet.orchestra.stage.model;

import java.util.function.Consumer;

public record StageOperations<T> (
        Consumer<T> processor,
        Consumer<T> afterResponseProcess,
        Consumer<T> afterResponseReceivedConsumer,
        Consumer<Exception> onError
        ){
}
