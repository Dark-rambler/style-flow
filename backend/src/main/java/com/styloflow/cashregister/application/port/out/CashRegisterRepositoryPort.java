package com.styloflow.cashregister.application.port.out;

import com.styloflow.cashregister.domain.model.CashRegister;
import com.styloflow.shared.domain.model.PageResult;
import java.util.Optional;

public interface CashRegisterRepositoryPort {

    /** @return the open cash register of the current business, or empty */
    Optional<CashRegister> findOpen();

    /**
     * @param id cash register id
     * @return the cash register, or empty
     */
    Optional<CashRegister> findById(Long id);

    /** Most recent first. */
    PageResult<CashRegister> findHistory(int page, int size);

    /**
     * @param cashRegister cash register to persist
     * @return the persisted cash register
     */
    CashRegister save(CashRegister cashRegister);
}
