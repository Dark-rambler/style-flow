package com.styloflow.cash.application.port.out;

import com.styloflow.cash.domain.model.CashModel;
import com.styloflow.shared.domain.model.PageResult;
import java.util.Optional;

public interface CashRepositoryPort {

    Optional<CashModel> findOpen();

    Optional<CashModel> findById(Long id);

    PageResult<CashModel> findHistory(int page, int size);

    CashModel save(CashModel cash);
}
