package com.campus.smartcampus.exception;

/** Thrown when the request clashes with existing data or a business rule (HTTP 409). */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
