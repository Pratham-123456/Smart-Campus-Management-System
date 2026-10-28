package com.campus.smartcampus.exception;

/** Thrown when the caller is authenticated but not allowed to touch this record (HTTP 403). */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
