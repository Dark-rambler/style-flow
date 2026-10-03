package com.styloflow.sales.application.port.in;

import com.styloflow.sales.application.port.in.command.RegisterSaleCommand;
import com.styloflow.sales.domain.model.Sale;
import com.styloflow.shared.domain.model.PageResult;
import java.time.LocalDate;

public interface SaleUseCase {

    Sale register(RegisterSaleCommand command, Long cashierId);

    Sale voidSale(Long id, String reason, Long userId);

    Sale get(Long id);

    PageResult<Sale> search(LocalDate from, LocalDate to, Long customerId, int page, int size);
}
