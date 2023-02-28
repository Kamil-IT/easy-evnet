package com.example.easyevnet.orchestra.stage.model;

import java.util.function.Consumer;

public record StageOperations<T> (
        Consumer<String> processor,
        Consumer<String> afterResponseProcess,
        Consumer<String> afterResponseReceivedConsumer,
        Consumer<Exception> onError
        ){
}
