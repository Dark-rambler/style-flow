package com.styloflow.cash.infrastructure.adapter.in.web;

import com.styloflow.cash.application.port.in.CashUseCase;
import com.styloflow.cash.infrastructure.adapter.in.web.dto.CashResponse;
import com.styloflow.cash.infrastructure.adapter.in.web.dto.CloseCashRequest;
import com.styloflow.cash.infrastructure.adapter.in.web.dto.OpenCashRequest;
import com.styloflow.shared.infrastructure.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/cash-register")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
@Tag(name = "Cash register", description = "Open, close and count the cash register")
public class CashController {

    private final CashUseCase cashUseCase;
    private final CashWebMapper cashMapper;

    @GetMapping("/current")
    @Operation(summary = "GET /api/cash-register/current — open cash register with live totals; 204 if none is open")
    public ResponseEntity<CashResponse> current() {
        return cashUseCase.current().map(cashMapper::toResponse).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/open")
    @Operation(summary = "POST /api/cash-register/open — open the cash register with an opening float")
    public ResponseEntity<CashResponse> open(@Valid @RequestBody OpenCashRequest request, Authentication auth) {
        return ResponseEntity.ok(cashMapper.toResponse(
                cashUseCase.open(cashMapper.toCommand(request), Long.parseLong(auth.getName()))));
    }

    @PostMapping("/close")
    @Operation(summary = "POST /api/cash-register/close — close the open cash register with the counted cash")
    public ResponseEntity<CashResponse> close(@Valid @RequestBody CloseCashRequest request, Authentication auth) {
        return ResponseEntity.ok(cashMapper.toResponse(
                cashUseCase.close(cashMapper.toCommand(request), Long.parseLong(auth.getName()))));
    }

    @GetMapping("/history")
    @Operation(summary = "GET /api/cash-register/history — cash register shifts, most recent first")
    public ResponseEntity<PageResponse<CashResponse>> history(@RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PageResponse.of(cashUseCase.history(page, size),
                cashMapper::toResponse));
    }

    @GetMapping("/{id}")
    @Operation(summary = "GET /api/cash-register/{id} — detail of a cash register shift")
    public ResponseEntity<CashResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(cashMapper.toResponse(cashUseCase.get(id)));
    }
}
