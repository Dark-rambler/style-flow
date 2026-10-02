package com.styloflow.shared.domain.exception;

/** The resource does not exist or belongs to another business (HTTP 404). */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String resource, Object id) {
        super(resource + " not found: " + id);
    }
}
