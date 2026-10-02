package com.styloflow.reports.application.port.in;

import com.styloflow.reports.domain.model.DailySales;
import com.styloflow.reports.domain.model.ExportedSale;
import com.styloflow.reports.domain.model.SalesSummary;
import com.styloflow.reports.domain.model.StylistTotal;
import com.styloflow.reports.domain.model.TopItem;
import com.styloflow.sales.domain.model.ItemType;
import com.styloflow.shared.domain.model.DateRange;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

/** Aggregated reports over COMPLETED sales, in the business time zone. */
public interface ReportUseCase {

    /** No dates = today; at most one year. */
    DateRange range(LocalDate from, LocalDate to);

    /**
     * @param range days to report
     * @return totals of completed sales in the range
     */
    SalesSummary summary(DateRange range);

    /**
     * @param range days to report
     * @return completed sales grouped by day
     */
    List<DailySales> salesByDay(DateRange range);

    /** {@code stylistId} null = every stylist. */
    List<StylistTotal> byStylist(DateRange range, Long stylistId);

    /**
     * @param range days to report
     * @param type services or products
     * @param limit maximum number of items
     * @return best sellers by amount
     */
    List<TopItem> topItems(DateRange range, ItemType type, int limit);

    /** Walks through the sales of the range (all of them, voided included) in chronological order. */
    void exportSales(DateRange range, Consumer<ExportedSale> consumer);
}
