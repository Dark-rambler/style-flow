package com.styloflow.catalog.infrastructure.adapter.in.web;

import com.styloflow.catalog.application.port.in.ProductUseCase;
import com.styloflow.catalog.infrastructure.adapter.in.web.dto.ProductRequest;
import com.styloflow.catalog.infrastructure.adapter.in.web.dto.ProductResponse;
import com.styloflow.catalog.infrastructure.adapter.in.web.dto.StockAdjustmentRequest;
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
@RequestMapping("/api/catalog/products")
@RequiredArgsConstructor
@Tag(name = "Catalog", description = "Categories, services and products")
public class ProductController {

    private final ProductUseCase productUseCase;
    private final CatalogWebMapper catalogMapper;

    @GetMapping
    @Operation(summary = "GET /api/catalog/products — list products")
    public ResponseEntity<List<ProductResponse>> list(@RequestParam(defaultValue = "false") boolean activeOnly) {
        return ResponseEntity.ok(catalogMapper.toProductResponseList(productUseCase.list(activeOnly)));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "GET /api/catalog/products/low-stock — active products at or below their minimum stock")
    public ResponseEntity<List<ProductResponse>> lowStock() {
        return ResponseEntity.ok(catalogMapper.toProductResponseList(productUseCase.lowStock()));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "POST /api/catalog/products — create a product (multipart: \"data\" JSON + optional \"image\")")
    public ResponseEntity<ProductResponse> create(@Valid @ModelAttribute ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(catalogMapper.toResponse(productUseCase.create(catalogMapper.toCommand(request))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "PUT /api/catalog/products/{id} — update a product")
    public ResponseEntity<ProductResponse> update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(catalogMapper.toResponse(productUseCase.update(id, catalogMapper.toCommand(request))));
    }

    @PostMapping("/{id}/stock-adjustment")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "POST /api/catalog/products/{id}/stock-adjustment — add or remove stock units")
    public ResponseEntity<ProductResponse> adjustStock(@PathVariable Long id,
            @Valid @RequestBody StockAdjustmentRequest request) {
        return ResponseEntity.ok(catalogMapper.toResponse(productUseCase.adjustStock(id, request.quantity())));
    }
}
