package com.example.easyevnet.monitor;

public interface EventListener<T> {

    void receive(T message);
}
