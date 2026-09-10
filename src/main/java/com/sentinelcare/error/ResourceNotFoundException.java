package com.sentinelcare.error;

/**
 * Thrown when a requested resource does not exist; mapped to 404 by the API advice.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}