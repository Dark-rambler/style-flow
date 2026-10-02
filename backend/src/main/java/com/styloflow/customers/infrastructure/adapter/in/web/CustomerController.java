package com.styloflow.customers.infrastructure.adapter.in.web;

import com.styloflow.customers.application.port.in.CustomerUseCase;
import com.styloflow.customers.infrastructure.adapter.in.web.dto.CustomerRequest;
import com.styloflow.customers.infrastructure.adapter.in.web.dto.CustomerResponse;
import com.styloflow.shared.infrastructure.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
@Tag(name = "Customers", description = "Customers of the business")
public class CustomerController {

    private final CustomerUseCase customerUseCase;
    private final CustomerWebMapper customerMapper;

    @GetMapping
    @Operation(summary = "GET /api/customers — search customers by name, phone or tax id")
    public ResponseEntity<PageResponse<CustomerResponse>> search(@RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PageResponse.of(customerUseCase.search(q, page, size), customerMapper::toResponse));
    }

    @GetMapping("/{id}")
    @Operation(summary = "GET /api/customers/{id} — get a customer by id")
    public ResponseEntity<CustomerResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(customerMapper.toResponse(customerUseCase.get(id)));
    }

    @PostMapping
    @Operation(summary = "POST /api/customers — create a customer")
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(customerMapper.toResponse(customerUseCase.create(customerMapper.toCommand(request))));
    }

    @PutMapping("/{id}")
    @Operation(summary = "PUT /api/customers/{id} — update a customer")
    public ResponseEntity<CustomerResponse> update(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(customerMapper.toResponse(customerUseCase.update(id, customerMapper.toCommand(request))));
    }
}
