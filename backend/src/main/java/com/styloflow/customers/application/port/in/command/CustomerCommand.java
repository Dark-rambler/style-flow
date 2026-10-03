package com.styloflow.customers.application.port.in.command;

public record CustomerCommand(
        String name,
        String phone,
        String email,
        String taxId,
        String notes
) {}
