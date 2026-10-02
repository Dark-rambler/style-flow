package com.styloflow.sales.application.port.out;

import com.styloflow.sales.domain.model.Sale;
import com.styloflow.shared.domain.model.PageResult;
import java.time.Instant;
import java.util.Optional;

public interface SaleRepositoryPort {

    /**
     * @param sale sale to persist
     * @return the persisted sale
     */
    Sale save(Sale sale);

    /** Sale with items, cash register and users. */
    Optional<Sale> findDetail(Long id);

    /** Most recent first; {@code to} is exclusive. */
    PageResult<Sale> search(Instant from, Instant to, Long customerId, int page, int size);
}
