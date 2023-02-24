package com.example.easyevnet.broker.kafka.model;

import com.example.easyevnet.orchestra.stage.model.Stage;

import java.util.Map;

public record ReceivedMessage<K>(K k, String body, Map<String, String> headers, Stage<String> stage) {
}
