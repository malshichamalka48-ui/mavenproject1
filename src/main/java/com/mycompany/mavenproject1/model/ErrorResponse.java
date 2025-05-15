package com.mycompany.mavenproject1.model;

/**
 * Standard error response body returned by all exception mappers.
 * Ensures consistent JSON error structure across the entire API.
 */
public class ErrorResponse {
    private int status;       // HTTP status code
    private String error;     // Short error category (e.g., "Conflict", "Not Found")
    private String message;   // Detailed human-readable explanation
    private long timestamp;   // When the error occurred (epoch ms)

    public ErrorResponse() {
    }

    public ErrorResponse(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
