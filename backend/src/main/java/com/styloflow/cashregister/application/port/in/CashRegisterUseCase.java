package com.styloflow.cashregister.application.port.in;

import com.styloflow.cashregister.application.port.in.command.CloseCashRegisterCommand;
import com.styloflow.cashregister.application.port.in.command.OpenCashRegisterCommand;
import com.styloflow.cashregister.domain.model.CashRegisterSummary;
import com.styloflow.shared.domain.model.PageResult;
import java.util.Optional;

public interface CashRegisterUseCase {

    Optional<CashRegisterSummary> current();

    CashRegisterSummary open(OpenCashRegisterCommand command, Long userId);

    CashRegisterSummary close(CloseCashRegisterCommand command, Long userId);

    PageResult<CashRegisterSummary> history(int page, int size);

    CashRegisterSummary get(Long id);
}
