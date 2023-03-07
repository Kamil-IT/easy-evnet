package com.example.easyevnet.monitor.api.event;

import java.util.ArrayList;
import java.util.List;

public class EventPublisher<T> {

    private static final EventPublisher<StageFinishedEvent> INSTANCE_FINISH_WORKFLOW = new EventPublisher<>();
    private static final EventPublisher<WorkflowFinishedWithErrorEvent> INSTANCE_FINISH_WITH_ERROR_WORKFLOW = new EventPublisher<>();

    private EventPublisher() {
    }

    public static EventPublisher<StageFinishedEvent> getInstanceStageFinished() {
        return INSTANCE_FINISH_WORKFLOW;
    }

    public static EventPublisher<WorkflowFinishedWithErrorEvent> getInstanceFinishWithErrorWorkflow() {
        return INSTANCE_FINISH_WITH_ERROR_WORKFLOW;
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
