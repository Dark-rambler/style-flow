package com.styloflow.cashregister.domain.exception;

import com.styloflow.shared.domain.exception.BusinessRuleException;

/** Thrown when an operation needs an open cash register and there is none. */
public class NoOpenCashRegisterException extends BusinessRuleException {

    /** Creates the exception with its standard message. */
    public NoOpenCashRegisterException() {
        super("There is no open cash register. Open one before selling.");
    }
}
