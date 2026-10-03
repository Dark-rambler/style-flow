package com.styloflow.sales.infrastructure.adapter.in.web;

import com.styloflow.sales.application.port.in.SaleUseCase;
import com.styloflow.sales.infrastructure.adapter.in.web.dto.SaleRequest;
import com.styloflow.sales.infrastructure.adapter.in.web.dto.SaleResponse;
import com.styloflow.sales.infrastructure.adapter.in.web.dto.SaleSummaryResponse;
import com.styloflow.sales.infrastructure.adapter.in.web.dto.VoidSaleRequest;
import com.styloflow.shared.infrastructure.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
@Tag(name = "Sales", description = "Checkout (POS) and sales history")
public class SaleController {

    private final SaleUseCase saleUseCase;
    private final SaleWebMapper saleMapper;

    @PostMapping
    @Operation(summary = "POST /api/sales — register a sale in the open cash register")
    public ResponseEntity<SaleResponse> register(@Valid @RequestBody SaleRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(saleMapper.toResponse(saleUseCase.register(saleMapper.toCommand(request), Long.parseLong(auth.getName()))));
    }

    @GetMapping
    @Operation(summary = "GET /api/sales — sales of a date range (today by default), optionally of a customer")
    public ResponseEntity<PageResponse<SaleSummaryResponse>> search(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long customerId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PageResponse.of(saleUseCase.search(from, to, customerId, page, size),
                saleMapper::toSummary));
    }

    @GetMapping("/{id}")
    @Operation(summary = "GET /api/sales/{id} — sale detail")
    public ResponseEntity<SaleResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(saleMapper.toResponse(saleUseCase.get(id)));
    }

    @PostMapping("/{id}/void")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "POST /api/sales/{id}/void — void a sale of the open cash register and restore stock")
    public ResponseEntity<SaleResponse> voidSale(@PathVariable Long id, @Valid @RequestBody VoidSaleRequest request, Authentication auth) {
        return ResponseEntity.ok(saleMapper.toResponse(saleUseCase.voidSale(id, request.reason(), Long.parseLong(auth.getName()))));
    }
}
