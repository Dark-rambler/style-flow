package com.styloflow.cashregister.application.port.in.command;

import java.math.BigDecimal;

public record CloseCashRegisterCommand(
        BigDecimal countedCash,
        String notes
) {}
