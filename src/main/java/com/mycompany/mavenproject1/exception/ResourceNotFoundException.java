package com.mycompany.mavenproject1.exception;

/**
 * Thrown when a requested resource (Room or Sensor) is not found in the data store.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
