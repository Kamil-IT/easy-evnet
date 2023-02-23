package com.example.easyevnet.bussines;

import com.example.easyevnet.config.producer.Publisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Configuration
public class ProduceMessageService {

    private final Publisher publisher;

    @Scheduled(cron = "* * * * * *")
    public void publishMessage() {
        publisher.sendMessage("Test message");
    }
}
