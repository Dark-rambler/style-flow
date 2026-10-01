package com.styloflow.common;

/** Violación de una regla de negocio (HTTP 422). */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
