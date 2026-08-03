package com.rushil.outreach.common.exception;

// Mapped to HTTP 409 by GlobalExceptionHandler
public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}