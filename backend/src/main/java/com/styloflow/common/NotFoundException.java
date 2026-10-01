package com.styloflow.common;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String recurso, Object id) {
        super(recurso + " no encontrado: " + id);
    }
}
