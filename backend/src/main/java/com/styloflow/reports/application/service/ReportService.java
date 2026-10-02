package com.styloflow.reports.application.service;

import com.styloflow.reports.application.port.in.ReportUseCase;
import com.styloflow.reports.application.port.out.ReportQueryPort;
import com.styloflow.reports.domain.model.DailySales;
import com.styloflow.reports.domain.model.ExportedSale;
import com.styloflow.reports.domain.model.SalesSummary;
import com.styloflow.reports.domain.model.StylistTotal;
import com.styloflow.reports.domain.model.TopItem;
import com.styloflow.sales.domain.model.ItemType;
import com.styloflow.shared.domain.exception.BusinessRuleException;
import com.styloflow.shared.domain.model.DateRange;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Sales reports of the current business; date ranges use the business time zone. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService implements ReportUseCase {

    private final ReportQueryPort reportQuery;
    private final Clock clock;
    private final ZoneId zone;

    @Override
    public DateRange range(LocalDate from, LocalDate to) {
        DateRange range = DateRange.of(from, to, LocalDate.now(clock.withZone(zone)));
        if (range.from().plusDays(366).isBefore(range.to())) {
            throw new BusinessRuleException("The maximum range is one year");
        }
        return range;
    }

    @Override
    public SalesSummary summary(DateRange range) {
        return SalesSummary.of(range, reportQuery.totals(range), reportQuery.totalsByPaymentMethod(range));
    }

    @Override
    public List<DailySales> salesByDay(DateRange range) {
        return reportQuery.salesByDay(range);
    }

    @Override
    public List<StylistTotal> byStylist(DateRange range, Long stylistId) {
        return reportQuery.byStylist(range, stylistId);
    }

    @Override
    public List<TopItem> topItems(DateRange range, ItemType type, int limit) {
        return reportQuery.topItems(range, type, Math.max(1, Math.min(limit, 50)));
    }

    @Override
    public void exportSales(DateRange range, Consumer<ExportedSale> consumer) {
        reportQuery.sales(range, consumer);
    }
}
