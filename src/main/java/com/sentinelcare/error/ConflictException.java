package com.sentinelcare.error;

/**
 * Thrown when an operation conflicts with existing data or retention rules;
 * mapped to 409 by the API advice.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}