package com.example.easyevnet.saga.source.stage;

public record StateMessage<ID>(ID id, String body) {
}
