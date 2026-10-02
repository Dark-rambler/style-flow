package com.styloflow.cashregister.infrastructure.adapter.in.web;

import com.styloflow.auth.infrastructure.security.CurrentUser;
import com.styloflow.cashregister.application.port.in.CashRegisterUseCase;
import com.styloflow.cashregister.infrastructure.adapter.in.web.dto.CashRegisterResponse;
import com.styloflow.cashregister.infrastructure.adapter.in.web.dto.CloseCashRegisterRequest;
import com.styloflow.cashregister.infrastructure.adapter.in.web.dto.OpenCashRegisterRequest;
import com.styloflow.shared.infrastructure.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
public class CashRegisterController {

    private final CashRegisterUseCase cashRegisterUseCase;
    private final CashRegisterWebMapper cashRegisterMapper;
    private final CurrentUser currentUser;

    @GetMapping("/current")
    @Operation(summary = "GET /api/cash-register/current — open cash register with live totals; 204 if none is open")
    public ResponseEntity<CashRegisterResponse> current() {
        return cashRegisterUseCase.current().map(cashRegisterMapper::toResponse).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/open")
    @Operation(summary = "POST /api/cash-register/open — open the cash register with an opening float")
    public ResponseEntity<CashRegisterResponse> open(@Valid @RequestBody OpenCashRegisterRequest request) {
        return ResponseEntity.ok(cashRegisterMapper.toResponse(
                cashRegisterUseCase.open(cashRegisterMapper.toCommand(request), currentUser.id())));
    }

    @PostMapping("/close")
    @Operation(summary = "POST /api/cash-register/close — close the open cash register with the counted cash")
    public ResponseEntity<CashRegisterResponse> close(@Valid @RequestBody CloseCashRegisterRequest request) {
        return ResponseEntity.ok(cashRegisterMapper.toResponse(
                cashRegisterUseCase.close(cashRegisterMapper.toCommand(request), currentUser.id())));
    }

    @GetMapping("/history")
    @Operation(summary = "GET /api/cash-register/history — cash register shifts, most recent first")
    public ResponseEntity<PageResponse<CashRegisterResponse>> history(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PageResponse.of(cashRegisterUseCase.history(page, size),
                cashRegisterMapper::toResponse));
    }

    @GetMapping("/{id}")
    @Operation(summary = "GET /api/cash-register/{id} — detail of a cash register shift")
    public ResponseEntity<CashRegisterResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(cashRegisterMapper.toResponse(cashRegisterUseCase.get(id)));
    }
}
