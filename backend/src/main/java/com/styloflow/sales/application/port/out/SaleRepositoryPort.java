package com.styloflow.sales.application.port.out;

import com.styloflow.sales.domain.model.SaleModel;
import com.styloflow.shared.domain.model.PageResult;
import java.time.Instant;
import java.util.Optional;

public interface SaleRepositoryPort {

    SaleModel save(SaleModel sale);

    Optional<SaleModel> findDetail(Long id);

    PageResult<SaleModel> search(Instant from, Instant to, Long customerId, int page, int size);
}
