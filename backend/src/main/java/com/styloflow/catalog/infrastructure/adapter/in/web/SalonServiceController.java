package com.styloflow.catalog.infrastructure.adapter.in.web;

import com.styloflow.catalog.application.port.in.SalonServiceUseCase;
import com.styloflow.catalog.infrastructure.adapter.in.web.dto.SalonServiceRequest;
import com.styloflow.catalog.infrastructure.adapter.in.web.dto.SalonServiceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/api/catalog/services")
@RequiredArgsConstructor
@Tag(name = "Catalog", description = "Categories, services and products")
public class SalonServiceController {

    private final SalonServiceUseCase serviceUseCase;
    private final CatalogWebMapper catalogMapper;

    @GetMapping
    @Operation(summary = "GET /api/catalog/services — list services; activeOnly also skips inactive categories")
    public ResponseEntity<List<SalonServiceResponse>> list(@RequestParam(defaultValue = "false") boolean activeOnly) {
        return ResponseEntity.ok(catalogMapper.toServiceResponseList(serviceUseCase.list(activeOnly)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "POST /api/catalog/services — create a service")
    public ResponseEntity<SalonServiceResponse> create(@Valid @RequestBody SalonServiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(catalogMapper.toResponse(serviceUseCase.create(catalogMapper.toCommand(request))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "PUT /api/catalog/services/{id} — update a service")
    public ResponseEntity<SalonServiceResponse> update(@PathVariable Long id,
            @Valid @RequestBody SalonServiceRequest request) {
        return ResponseEntity.ok(catalogMapper.toResponse(serviceUseCase.update(id, catalogMapper.toCommand(request))));
    }
}
