package com.styloflow.sales.application.port.out;

import com.styloflow.sales.domain.model.Sale;
import com.styloflow.shared.domain.model.PageResult;
import java.time.Instant;
import java.util.Optional;

public interface SaleRepositoryPort {

    Sale save(Sale sale);

    Optional<Sale> findDetail(Long id);

    PageResult<Sale> search(Instant from, Instant to, Long customerId, int page, int size);
}
