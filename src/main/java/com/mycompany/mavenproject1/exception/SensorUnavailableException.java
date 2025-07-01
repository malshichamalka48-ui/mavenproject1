package com.mycompany.mavenproject1.exception;

/**
 * Thrown when a sensor in "MAINTENANCE" mode is unable to accept new readings.
 * The sensor is physically disconnected and cannot process data.
 */
public class SensorUnavailableException extends RuntimeException {
    public SensorUnavailableException(String message) {
        super(message);
    }
}
