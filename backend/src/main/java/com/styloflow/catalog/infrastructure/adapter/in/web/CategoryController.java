package com.styloflow.catalog.infrastructure.adapter.in.web;

import com.styloflow.catalog.application.port.in.CategoryUseCase;
import com.styloflow.catalog.infrastructure.adapter.in.web.dto.CategoryRequest;
import com.styloflow.catalog.infrastructure.adapter.in.web.dto.CategoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Read: any authenticated user. Write: ADMIN only. */
@RestController
@RequestMapping("/api/catalog/categories")
@RequiredArgsConstructor
@Tag(name = "Catalog", description = "Categories, services and products")
public class CategoryController {

    private final CategoryUseCase categoryUseCase;
    private final CatalogWebMapper catalogMapper;

    @GetMapping
    @Operation(summary = "GET /api/catalog/categories — list categories")
    public ResponseEntity<List<CategoryResponse>> list() {
        return ResponseEntity.ok(catalogMapper.toCategoryResponseList(categoryUseCase.list()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "POST /api/catalog/categories — create a category")
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(catalogMapper.toResponse(categoryUseCase.create(catalogMapper.toCommand(request))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "PUT /api/catalog/categories/{id} — update a category")
    public ResponseEntity<CategoryResponse> update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(catalogMapper.toResponse(categoryUseCase.update(id, catalogMapper.toCommand(request))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "DELETE /api/catalog/categories/{id} — delete a category without services")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
