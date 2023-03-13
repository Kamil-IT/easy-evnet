package com.example.easyevnet.monitor.api;

import com.example.easyevnet.monitor.api.model.ResponseList;
import com.example.easyevnet.monitor.api.database.OrchestraMonitorRepository;
import com.example.easyevnet.monitor.api.database.StageMonitorRepository;
import com.example.easyevnet.monitor.audit.database.model.OrchestraPersistence;
import com.example.easyevnet.monitor.audit.database.model.StagePersistence;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RequiredArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("api/v1/easyevent/monitor")
public class MonitoringApi {

    private final StageMonitorRepository stageRepository;
    private final OrchestraMonitorRepository orchestraMonitorRepository;

    @GetMapping("stage")
    public ResponseList<StagePersistence> stage() {
        return new ResponseList<>(stageRepository.findAll());
    }

    @GetMapping("orchestra")
    public ResponseList<OrchestraPersistence> orchestra() {
        return new ResponseList<>(orchestraMonitorRepository.findAll());
    }

    @PostMapping("messages/send")
    public SendResult<String, String> sendMessage(String broker, String topic,
                                                String key, String val,
                                                Map<String, String> headers) throws ExecutionException, InterruptedException, TimeoutException {

        var record = new ProducerRecord<>(topic, key, val);
        headers.forEach((key1, value) ->
                record.headers()
                        .add(new DeadLetterPublishingRecoverer.SingleRecordHeader(key1, value.getBytes())));

        KafkaTemplate<String, String> kaf = getKafkaTemplate(broker);

        CompletableFuture<SendResult<String, String>> send = kaf.send(record);
        return send.get(10, TimeUnit.SECONDS);
    }

    private static KafkaTemplate<String, String> getKafkaTemplate(String broker) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, broker);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(configProps));
    }
}
