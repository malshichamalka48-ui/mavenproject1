package com.mycompany.mavenproject1.exception;

/**
 * Thrown when a request body references a linked resource (e.g., roomId) that does not exist.
 * For example, registering a sensor with a non-existent roomId.
 */
public class LinkedResourceNotFoundException extends RuntimeException {
    public LinkedResourceNotFoundException(String message) {
        super(message);
    }
}
