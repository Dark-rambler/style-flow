package com.styloflow.cash.application.port.in.command;

import java.math.BigDecimal;

public record OpenCashCommand(
        BigDecimal openingAmount,
        String notes
) {}
