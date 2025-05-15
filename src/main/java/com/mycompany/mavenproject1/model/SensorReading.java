package com.mycompany.mavenproject1.model;

import java.util.UUID;

/**
 * Represents a single reading event captured by a sensor.
 * Each reading has a unique UUID, a timestamp, and the recorded value.
 */
public class SensorReading {
    private String id;       // Unique reading event ID (UUID)
    private long timestamp;  // Epoch time (ms) when the reading was captured
    private double value;    // The actual metric value recorded by the hardware

    public SensorReading() {
        this.id = UUID.randomUUID().toString();
    }

    public SensorReading(long timestamp, double value) {
        this.id = UUID.randomUUID().toString();
        this.timestamp = timestamp;
        this.value = value;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
