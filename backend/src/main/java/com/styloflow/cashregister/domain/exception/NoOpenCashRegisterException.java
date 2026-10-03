package com.styloflow.cashregister.domain.exception;

import com.styloflow.shared.domain.exception.BusinessRuleException;

public class NoOpenCashRegisterException extends BusinessRuleException {

    public NoOpenCashRegisterException() {
        super("There is no open cash register. Open one before selling.");
    }
}
