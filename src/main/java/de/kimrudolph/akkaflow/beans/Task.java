package de.kimrudolph.akkaflow.beans;

/**
 * Payload container
 */
public class Task {

    private String payload;

    private Integer priority;

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }
}
