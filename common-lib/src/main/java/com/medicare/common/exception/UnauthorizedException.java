package com.medicare.common.exception;

/** Thrown when an authenticated user lacks permission for an action. Maps to HTTP 403. */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
