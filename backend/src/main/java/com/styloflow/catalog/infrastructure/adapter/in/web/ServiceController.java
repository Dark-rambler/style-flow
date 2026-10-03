package com.styloflow.catalog.infrastructure.adapter.in.web;

import com.styloflow.catalog.application.port.in.ServiceUseCase;
import com.styloflow.catalog.infrastructure.adapter.in.web.dto.ServiceRequest;
import com.styloflow.catalog.infrastructure.adapter.in.web.dto.ServiceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/catalog/services")
@RequiredArgsConstructor
@Tag(name = "Catalog", description = "Categories, services and products")
public class ServiceController {

    private final ServiceUseCase serviceUseCase;
    private final CatalogWebMapper catalogMapper;

    @GetMapping
    @Operation(summary = "GET /api/catalog/services — list services; activeOnly also skips inactive categories")
    public ResponseEntity<List<ServiceResponse>> list(@RequestParam(defaultValue = "false") boolean activeOnly) {
        return ResponseEntity.ok(catalogMapper.toServiceResponseList(serviceUseCase.list(activeOnly)));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "POST /api/catalog/services — create a service (multipart: \"data\" JSON + optional \"image\")")
    public ResponseEntity<ServiceResponse> create(@Valid @ModelAttribute ServiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(catalogMapper.toResponse(serviceUseCase.create(catalogMapper.toCommand(request))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "PUT /api/catalog/services/{id} — update a service")
    public ResponseEntity<ServiceResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody ServiceRequest request) {
        return ResponseEntity.ok(catalogMapper.toResponse(serviceUseCase.update(id, catalogMapper.toCommand(request))));
    }
}
