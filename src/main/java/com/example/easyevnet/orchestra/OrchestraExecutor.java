package com.example.easyevnet.orchestra;

import com.example.easyevnet.broker.kafka.config.KafkaListenerConfig;
import com.example.easyevnet.orchestra.StageExecutor;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.StreamSupport;

@RequiredArgsConstructor
public class OrchestraExecutor<ID> {

    private final StageExecutor<ID> stageExecutor;
    private final KafkaListenerConfig kafkaListenerConfig;

    public boolean startOrchestra() {
        List<String> topics = stageExecutor.getTopics();

        kafkaListenerConfig.createStartedConsumer(topics, this::processNextStep);

        return true;
    }

    private void processNextStep(ConsumerRecord<String, String> rec) {
        stageExecutor.processNextStep(getStage(rec), rec.value());
    }

    private static String getStage(ConsumerRecord<String, String> rec) {
        return StreamSupport.stream(rec.headers().headers("stage").spliterator(), false)
                .findFirst()
                .map(Header::value)
                .map(stage -> new String(stage, StandardCharsets.UTF_8))
                .orElse("CHECK_PAYMENT");
    }
}
