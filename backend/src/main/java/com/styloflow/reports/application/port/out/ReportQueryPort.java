package com.styloflow.reports.application.port.out;

import com.styloflow.reports.domain.model.DailySales;
import com.styloflow.reports.domain.model.ExportedSale;
import com.styloflow.reports.domain.model.PaymentMethodTotal;
import com.styloflow.reports.domain.model.SalesTotals;
import com.styloflow.reports.domain.model.StylistTotal;
import com.styloflow.reports.domain.model.TopItem;
import com.styloflow.sales.domain.model.ItemType;
import com.styloflow.shared.domain.model.DateRange;
import java.util.List;
import java.util.function.Consumer;

/** Report queries of the current business. */
public interface ReportQueryPort {

    SalesTotals totals(DateRange range);

    /**
     * @param range days to report
     * @return completed sales grouped by payment method
     */
    List<PaymentMethodTotal> totalsByPaymentMethod(DateRange range);

    /**
     * @param range days to report
     * @return completed sales grouped by day
     */
    List<DailySales> salesByDay(DateRange range);

    /**
     * @param range days to report
     * @param stylistId stylist to filter by, or {@code null} for all
     * @return service sales and commissions per stylist
     */
    List<StylistTotal> byStylist(DateRange range, Long stylistId);

    /**
     * @param range days to report
     * @param type services or products
     * @param limit maximum number of items
     * @return best sellers by amount
     */
    List<TopItem> topItems(DateRange range, ItemType type, int limit);

    /**
     * Streams the sales of the range (CSV export) without loading them all in memory.
     *
     * @param range days to export
     * @param consumer receives each sale
     */
    void sales(DateRange range, Consumer<ExportedSale> consumer);
}
