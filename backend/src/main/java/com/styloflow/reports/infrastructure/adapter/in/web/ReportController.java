package com.styloflow.reports.infrastructure.adapter.in.web;

import com.styloflow.reports.application.port.in.ReportUseCase;
import com.styloflow.reports.infrastructure.adapter.in.web.dto.DailySalesResponse;
import com.styloflow.reports.infrastructure.adapter.in.web.dto.SalesSummaryResponse;
import com.styloflow.reports.infrastructure.adapter.in.web.dto.StylistTotalResponse;
import com.styloflow.reports.infrastructure.adapter.in.web.dto.TopItemResponse;
import com.styloflow.sales.domain.enums.ItemType;
import com.styloflow.shared.domain.model.DateRange;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Sales, commissions and export")
public class ReportController {

    private final ReportUseCase reportUseCase;
    private final ReportWebMapper reportMapper;
    private final SalesCsvWriter csvWriter;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    @Operation(summary = "GET /api/reports/summary — totals of the range: sales, average ticket, discounts, tax and "
            + "payment methods")
    public ResponseEntity<SalesSummaryResponse> summary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reportMapper.toResponse(reportUseCase.summary(reportUseCase.range(from, to))));
    }

    @GetMapping("/sales-by-day")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "GET /api/reports/sales-by-day — sales grouped by day")
    public ResponseEntity<List<DailySalesResponse>> salesByDay(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reportMapper.toDailySalesList(reportUseCase.salesByDay(reportUseCase.range(from, to))));
    }

    @GetMapping("/stylists")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "GET /api/reports/stylists — production and commission of every stylist")
    public ResponseEntity<List<StylistTotalResponse>> byStylist(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reportMapper.toStylistList(
                reportUseCase.byStylist(reportUseCase.range(from, to), null)));
    }

    @GetMapping("/my-commissions")
    @Operation(summary = "GET /api/reports/my-commissions — production and commission of the authenticated stylist")
    public ResponseEntity<List<StylistTotalResponse>> myCommissions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication auth) {
        return ResponseEntity.ok(reportMapper.toStylistList(
                reportUseCase.byStylist(reportUseCase.range(from, to), Long.parseLong(auth.getName()))));
    }

    @GetMapping("/top")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "GET /api/reports/top — best-selling services or products")
    public ResponseEntity<List<TopItemResponse>> top(@RequestParam(defaultValue = "SERVICE") ItemType type,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reportMapper.toTopItemList(
                reportUseCase.topItems(reportUseCase.range(from, to), type, limit)));
    }

    @GetMapping("/sales.csv")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "GET /api/reports/sales.csv — export the sales of the range as CSV")
    public void exportCsv(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletResponse response) throws IOException {
        DateRange range = reportUseCase.range(from, to);
        response.setContentType("text/csv");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"sales_" + range.from() + "_" + range.to() + ".csv\"");
        PrintWriter out = response.getWriter();
        csvWriter.header(out);
        reportUseCase.exportSales(range, sale -> csvWriter.row(out, sale));
        out.flush();
    }
}
