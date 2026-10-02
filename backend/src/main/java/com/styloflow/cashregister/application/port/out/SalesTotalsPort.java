package com.styloflow.cashregister.application.port.out;

import com.styloflow.cashregister.domain.model.PaymentTotal;
import java.util.List;

/** Totals of the completed sales of a cash register; implemented by the sales persistence. */
public interface SalesTotalsPort {

    List<PaymentTotal> totalsByPaymentMethod(Long cashRegisterId);
}
