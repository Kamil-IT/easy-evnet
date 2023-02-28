package com.example.easyevnet.broker.kafka.model;

import com.example.easyevnet.orchestra.stage.model.StageData;

import java.util.Map;

public record ProducedMessage<K>(K k, String body, Map<String, String> headers, StageData<String> stageData) {
}
