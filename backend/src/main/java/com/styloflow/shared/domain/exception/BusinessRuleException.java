package com.styloflow.shared.domain.exception;

/** A business rule was violated (HTTP 422). */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
