package com.styloflow.cashregister.application.port.in;

import com.styloflow.cashregister.domain.model.CashRegisterSummary;
import com.styloflow.shared.domain.model.PageResult;
import java.util.Optional;

/** Only one cash register can be open at a time per business. */
public interface CashRegisterUseCase {

    /** @return summary of the open cash register, or empty */
    Optional<CashRegisterSummary> current();

    /**
     * Opens a new cash register.
     *
     * @param command opening float and notes
     * @param userId user opening the cash register
     * @return summary of the new cash register
     * @throws com.styloflow.shared.domain.exception.BusinessRuleException if another one is already open
     */
    CashRegisterSummary open(OpenCashRegisterCommand command, Long userId);

    /**
     * Closes the open cash register with a cash count.
     *
     * @param command counted cash and notes
     * @param userId user closing the cash register
     * @return summary with the expected cash and the difference
     * @throws com.styloflow.cashregister.domain.exception.NoOpenCashRegisterException if none is open
     */
    CashRegisterSummary close(CloseCashRegisterCommand command, Long userId);

    /**
     * Lists cash registers, newest first.
     *
     * @param page zero-based page
     * @param size page size (capped at 100)
     * @return a page of summaries
     */
    PageResult<CashRegisterSummary> history(int page, int size);

    /**
     * @param id cash register id
     * @return its summary
     * @throws com.styloflow.shared.domain.exception.NotFoundException if it does not exist
     */
    CashRegisterSummary get(Long id);
}
