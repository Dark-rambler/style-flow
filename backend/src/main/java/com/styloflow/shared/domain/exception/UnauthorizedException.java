package com.styloflow.shared.domain.exception;

/** Invalid credentials or session (HTTP 401). */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
