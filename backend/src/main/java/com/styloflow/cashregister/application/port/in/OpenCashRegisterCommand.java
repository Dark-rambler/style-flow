package com.styloflow.cashregister.application.port.in;

import java.math.BigDecimal;

/**
 * Opens a cash register shift.
 *
 * @param openingAmount cash float at the start of the shift
 * @param notes optional notes
 */
public record OpenCashRegisterCommand(BigDecimal openingAmount, String notes) {}
