package com.styloflow.cash.application.port.in.command;

import java.math.BigDecimal;

public record CloseCashCommand(
        BigDecimal countedCash,
        String notes
) {}
