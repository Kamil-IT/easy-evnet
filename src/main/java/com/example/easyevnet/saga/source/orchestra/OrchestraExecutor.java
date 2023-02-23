package com.example.easyevnet.saga.source.orchestra;

import com.example.easyevnet.bussines.ShopEventType;
import com.example.easyevnet.saga.source.stage.StageExecutor;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import java.time.Duration;
import java.util.stream.StreamSupport;

import static com.example.easyevnet.saga.source.config.kafka.KafkaListinerCOnfig.createConsumer;

@RequiredArgsConstructor
public class OrchestraExecutor<ID> {

    private final StageExecutor<ID> stageExecutor;

    public boolean startOrchestra() {
        Consumer<Long, String> consumer = createConsumer(stageExecutor.getTopics());

        startListening(consumer);

        return true;
    }

    private void startListening(Consumer<Long, String> consumer) {
        while (!stageExecutor.isLastStageDone()) {
            ConsumerRecords<Long, String> poll = consumer.poll(Duration.ofSeconds(1)); // Return always 0 or 1 record

            Iterable<ConsumerRecord<Long, String>> records = poll.records(stageExecutor.getNextOrderedStage().queueName());
            StreamSupport.stream(records.spliterator(), false)
                    .findFirst()
                    .ifPresent(this::processNextStep);
        }
    }

    private boolean processNextStep(ConsumerRecord<Long, String> rec) {
        try {
            return stageExecutor.processNextStep(ShopEventType.CREATE_ORDER.toString(), rec.value());
        } catch (Exception e) {

            return processNextStep(rec);
        }
    }
}
