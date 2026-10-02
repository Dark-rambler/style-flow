package com.styloflow.platform.infrastructure.adapter.in.web;

import com.styloflow.platform.application.port.in.PlatformUseCase;
import com.styloflow.platform.infrastructure.adapter.in.web.dto.BusinessSummaryResponse;
import com.styloflow.platform.infrastructure.adapter.in.web.dto.CreateBusinessRequest;
import com.styloflow.platform.infrastructure.adapter.in.web.dto.StatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Requires the SUPERADMIN role (see SecurityConfig); the login lives in {@code PlatformAuthController}. */
@RestController
@RequestMapping("/api/platform/businesses")
@RequiredArgsConstructor
@Tag(name = "Platform", description = "Business administration (superadmin)")
public class PlatformController {

    private final PlatformUseCase platformUseCase;
    private final PlatformWebMapper platformMapper;

    @GetMapping
    @Operation(summary = "GET /api/platform/businesses — list businesses with their 30-day activity")
    public ResponseEntity<List<BusinessSummaryResponse>> list() {
        return ResponseEntity.ok(platformMapper.toResponseList(platformUseCase.list()));
    }

    @PostMapping
    @Operation(summary = "POST /api/platform/businesses — create a business with its administrator")
    public ResponseEntity<BusinessSummaryResponse> create(@Valid @RequestBody CreateBusinessRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(platformMapper.toResponse(platformUseCase.create(platformMapper.toCommand(request))));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "PUT /api/platform/businesses/{id}/status — activate or suspend a business")
    public ResponseEntity<Void> changeStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
        platformUseCase.changeStatus(id, request.active());
        return ResponseEntity.noContent().build();
    }
}
