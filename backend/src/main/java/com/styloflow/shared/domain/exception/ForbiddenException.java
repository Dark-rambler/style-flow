package com.styloflow.shared.domain.exception;

/** The caller is not allowed to perform the operation (HTTP 403). */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
