package com.mycompany.mavenproject1.exception;

/**
 * Thrown when attempting to delete a Room that still has sensors assigned to it.
 * This prevents orphaned sensor data in the system.
 */
public class RoomNotEmptyException extends RuntimeException {
    public RoomNotEmptyException(String message) {
        super(message);
    }
}
