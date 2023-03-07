package com.example.easyevnet.monitor.api.event;

public interface EventListener<T> {

    void receive(T message);
}
