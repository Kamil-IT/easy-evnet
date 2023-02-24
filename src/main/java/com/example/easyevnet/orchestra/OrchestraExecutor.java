package com.example.easyevnet.orchestra;

import com.example.app.bussines.ShopEventType;
import com.example.easyevnet.broker.kafka.config.KafkaListenerConfig;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class OrchestraExecutor<ID> {

    private final StageExecutor<ID> stageExecutor;
    private final KafkaListenerConfig kafkaListenerConfig;

    public boolean startOrchestra() {

        List<String> topics = Arrays.asList("test.topic", stageExecutor.getNextOrderedStage().queueName());

        kafkaListenerConfig.createStartedConsumer(topics, this::processNextStep);

        return true;
    }

    private boolean processNextStep(ConsumerRecord<String, String> rec) {
        return stageExecutor.processNextStep(ShopEventType.CREATE_ORDER.toString(), rec.value());
    }
}
