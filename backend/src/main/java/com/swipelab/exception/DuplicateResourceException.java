package com.swipelab.exception;

/**
 * Thrown when a resource that already exists is created again.
 * Maps to HTTP 409 CONFLICT via GlobalExceptionHandler.
 */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
