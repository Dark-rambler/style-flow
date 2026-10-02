package com.styloflow.customers.application.port.in;

/**
 * Data to create or update a customer.
 *
 * @param name full name
 * @param phone optional phone
 * @param email optional email
 * @param taxId optional tax id for invoices
 * @param notes optional notes
 */
public record CustomerCommand(String name, String phone, String email, String taxId, String notes) {}
