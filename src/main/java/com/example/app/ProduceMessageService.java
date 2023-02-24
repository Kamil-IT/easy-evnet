package com.example.app;

import com.example.app.config.producer.Publisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@RequiredArgsConstructor
@EnableScheduling
@Configuration
public class ProduceMessageService {

    private final Publisher publisher;

    @Scheduled(cron = "* * * * * *")
    public void publishMessage() {
//        com.example.app.bussines.ShopEventType.CANCEL_ORDER
        publisher.sendMessage(Publisher.TOPIC_NAME, "Test message");
        publisher.sendMessage("com.example.app.bussines.ShopEventType.CANCEL_ORDER", "Test message");
    }
}
