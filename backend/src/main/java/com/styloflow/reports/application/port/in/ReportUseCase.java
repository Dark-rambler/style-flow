package com.styloflow.reports.application.port.in;

import com.styloflow.reports.domain.model.DailySales;
import com.styloflow.reports.domain.model.ExportedSale;
import com.styloflow.reports.domain.model.SalesSummary;
import com.styloflow.reports.domain.model.StylistTotal;
import com.styloflow.reports.domain.model.TopItem;
import com.styloflow.sales.domain.enums.ItemType;
import com.styloflow.shared.domain.model.DateRange;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

public interface ReportUseCase {

    DateRange range(LocalDate from, LocalDate to);

    SalesSummary summary(DateRange range);

    List<DailySales> salesByDay(DateRange range);

    List<StylistTotal> byStylist(DateRange range, Long stylistId);

    List<TopItem> topItems(DateRange range, ItemType type, int limit);

    void exportSales(DateRange range, Consumer<ExportedSale> consumer);
}
