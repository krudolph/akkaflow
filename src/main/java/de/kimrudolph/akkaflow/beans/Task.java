package de.kimrudolph.akkaflow.beans;

/**
 * Payload container
 */
public class Task {

    private final String payload;

    private final Integer priority;
    
    public Task(final String payload, final Integer priority) {
        this.payload = payload;
        this.priority = priority;
    }

    public String getPayload() {
        return payload;
    }

    public Integer getPriority() {
        return priority;
    }
}
