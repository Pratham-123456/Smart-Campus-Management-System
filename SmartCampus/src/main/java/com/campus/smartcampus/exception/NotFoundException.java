package com.campus.smartcampus.exception;

/** Thrown when a requested record does not exist (HTTP 404). */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
