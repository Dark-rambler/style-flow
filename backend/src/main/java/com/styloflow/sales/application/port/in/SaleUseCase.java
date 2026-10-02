package com.styloflow.sales.application.port.in;

import com.styloflow.sales.domain.model.Sale;
import com.styloflow.shared.domain.model.PageResult;
import java.time.LocalDate;

/** Point of sale: registering, voiding and querying sales. */
public interface SaleUseCase {

    /** Requires an open cash register; products decrease stock. */
    Sale register(RegisterSaleCommand command, Long cashierId);

    /** Restores the stock of the products. */
    Sale voidSale(Long id, String reason, Long userId);

    /**
     * @param id sale id
     * @return the sale with its items
     * @throws com.styloflow.shared.domain.exception.NotFoundException if it does not exist
     */
    Sale get(Long id);

    /**
     * Searches sales, newest first.
     *
     * @param from first day, or {@code null}
     * @param to last day, or {@code null}
     * @param customerId customer to filter by, or {@code null}
     * @param page zero-based page
     * @param size page size
     * @return a page of sales
     */
    PageResult<Sale> search(LocalDate from, LocalDate to, Long customerId, int page, int size);
}
