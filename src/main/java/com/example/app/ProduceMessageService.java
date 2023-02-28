package com.example.app;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer.SingleRecordHeader;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Profile("!test")
@RequiredArgsConstructor
@EnableScheduling
@Configuration
public class ProduceMessageService {

    private final KafkaTemplate<String, String> kafkaTemplate;

//    @Scheduled(cron = "* * * * * *")
    @PostConstruct
    public void publishMessage() throws InterruptedException {
//        var record = new ProducerRecord<>("com.example.app.bussines.ShopEventType.CREATE_ORDER", "1001", "Hello World");
//        record.headers()
//                .add(new SingleRecordHeader("stage", "CREATE_ORDER".getBytes()));
//        kafkaTemplate.send(record);
//
//        TimeUnit.SECONDS.sleep(1);
//
//
//        var record2 = new ProducerRecord<>("com.example.app.bussines.ShopEventType.CHECK_PAYMENT", "1001", "Hello World");
//        record2.headers()
//                .add(new SingleRecordHeader("stage", "CHECK_PAYMENT".getBytes()));
//        kafkaTemplate.send(record2);
//
//        TimeUnit.SECONDS.sleep(1);
//
//        var record3 = new ProducerRecord<>("com.example.app.bussines.ShopEventType.CANCEL_ORDER", "1001", "Hello World");
//        record3.headers()
//                .add(new SingleRecordHeader("stage", "CANCEL_ORDER".getBytes()));
//        kafkaTemplate.send(record3);
    }
}
