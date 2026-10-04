package com.styloflow.cash.domain.exception;

import com.styloflow.shared.domain.exception.BusinessRuleException;

public class NoOpenCashException extends BusinessRuleException {

    public NoOpenCashException() {
        super("There is no open cash register. Open one before selling.");
    }
}
