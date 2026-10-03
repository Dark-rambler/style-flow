package com.styloflow.catalog.application.port.in.command;

public record CategoryCommand(
        String name,
        Boolean active
) {}
