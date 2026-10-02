package com.styloflow.catalog.application.port.in;

/**
 * Data to create or update a category.
 *
 * @param name category name (unique per business)
 * @param active new active flag, or {@code null} to keep it
 */
public record CategoryCommand(String name, Boolean active) {}
