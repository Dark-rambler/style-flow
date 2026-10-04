package com.styloflow.reports.application.port.out;

import com.styloflow.reports.domain.model.DailySales;
import com.styloflow.reports.domain.model.ExportedSale;
import com.styloflow.reports.domain.model.PaymentMethodTotal;
import com.styloflow.reports.domain.model.SalesTotals;
import com.styloflow.reports.domain.model.StylistTotal;
import com.styloflow.reports.domain.model.TopItem;
import com.styloflow.sales.domain.enums.ItemType;
import com.styloflow.shared.domain.model.DateRange;
import java.util.List;
import java.util.function.Consumer;

public interface ReportQueryPort {

    SalesTotals totals(DateRange range);

    List<PaymentMethodTotal> totalsByPaymentMethod(DateRange range);

    List<DailySales> salesByDay(DateRange range);

    List<StylistTotal> byStylist(DateRange range, Long stylistId);

    List<TopItem> topItems(DateRange range, ItemType type, int limit);

    void sales(DateRange range, Consumer<ExportedSale> consumer);
}
