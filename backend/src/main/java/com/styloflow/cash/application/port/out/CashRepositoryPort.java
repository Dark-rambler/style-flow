package com.styloflow.cash.application.port.out;

import com.styloflow.cash.domain.model.Cash;
import com.styloflow.shared.domain.model.PageResult;
import java.util.Optional;

public interface CashRepositoryPort {

    Optional<Cash> findOpen();

    Optional<Cash> findById(Long id);

    PageResult<Cash> findHistory(int page, int size);

    Cash save(Cash cash);
}
