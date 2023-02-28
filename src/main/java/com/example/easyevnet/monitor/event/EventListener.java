package com.example.easyevnet.monitor.event;

public interface EventListener<T> {

    void receive(T message);
}
