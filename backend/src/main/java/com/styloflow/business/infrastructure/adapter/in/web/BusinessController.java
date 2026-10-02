package com.styloflow.business.infrastructure.adapter.in.web;

import com.styloflow.business.application.port.in.BusinessUseCase;
import com.styloflow.business.infrastructure.adapter.in.web.dto.BusinessRequest;
import com.styloflow.business.infrastructure.adapter.in.web.dto.BusinessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/business")
@RequiredArgsConstructor
@Tag(name = "Business", description = "Settings of the current business")
public class BusinessController {

    private final BusinessUseCase businessUseCase;
    private final BusinessWebMapper businessMapper;

    @GetMapping
    @Operation(summary = "GET /api/business — current business settings (receipt, currency, tax)")
    public ResponseEntity<BusinessResponse> get() {
        return ResponseEntity.ok(businessMapper.toResponse(businessUseCase.getCurrent()));
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "PUT /api/business — update the current business settings")
    public ResponseEntity<BusinessResponse> update(@Valid @RequestBody BusinessRequest request) {
        return ResponseEntity.ok(businessMapper.toResponse(businessUseCase.update(businessMapper.toCommand(request))));
    }
}
