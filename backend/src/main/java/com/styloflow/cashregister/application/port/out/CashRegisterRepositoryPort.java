package com.styloflow.cashregister.application.port.out;

import com.styloflow.cashregister.domain.model.CashRegister;
import com.styloflow.shared.domain.model.PageResult;
import java.util.Optional;

public interface CashRegisterRepositoryPort {

    Optional<CashRegister> findOpen();

    Optional<CashRegister> findById(Long id);

    PageResult<CashRegister> findHistory(int page, int size);

    CashRegister save(CashRegister cashRegister);
}
