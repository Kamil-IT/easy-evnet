package com.example.easyevnet.monitor;

import java.util.ArrayList;
import java.util.List;

public class EventPublisher<T> {

    private static final EventPublisher<WorkflowFinishedEvent> INSTANCE = new EventPublisher<>();

    private EventPublisher() {
    }

    public static EventPublisher<WorkflowFinishedEvent> getInstanceWorkflowFinished() {
        return INSTANCE;
    }

    private final List<EventListener<T>> listeners = new ArrayList<>();

    public void publish(T event) {
        listeners.forEach(l -> l.receive(event));
    }

    public void subscribe(EventListener<T> listener) {
        listeners.add(listener);
    }

    public void unsubscribe(EventListener<T> listener) {
        listeners.remove(listener);
    }
}
