package com.styloflow.cash.application.port.in;

import com.styloflow.cash.application.port.in.command.CloseCashCommand;
import com.styloflow.cash.application.port.in.command.OpenCashCommand;
import com.styloflow.cash.domain.model.CashSummary;
import com.styloflow.shared.domain.model.PageResult;
import java.util.Optional;

public interface CashUseCase {

    Optional<CashSummary> current();

    CashSummary open(OpenCashCommand command, Long userId);

    CashSummary close(CloseCashCommand command, Long userId);

    PageResult<CashSummary> history(int page, int size);

    CashSummary get(Long id);
}
