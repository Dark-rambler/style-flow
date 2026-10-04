package com.styloflow.sales.application.port.in;

import com.styloflow.sales.application.port.in.command.RegisterSaleCommand;
import com.styloflow.sales.domain.model.SaleModel;
import com.styloflow.shared.domain.model.PageResult;
import java.time.LocalDate;

public interface SaleUseCase {

    SaleModel register(RegisterSaleCommand command, Long cashierId);

    SaleModel voidSale(Long id, String reason, Long userId);

    SaleModel get(Long id);

    PageResult<SaleModel> search(LocalDate from, LocalDate to, Long customerId, int page, int size);
}
