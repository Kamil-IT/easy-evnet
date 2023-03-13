package com.example.easyevnet.broker.kafka.model;

import com.example.easyevnet.orchestra.stage.model.Stage;
import com.example.easyevnet.orchestra.stage.model.StageData;

import java.util.Map;

public record ReceivedMessage<K, T>(K k, T body, Map<String, String> headers, Stage<T> stageData) {
}
