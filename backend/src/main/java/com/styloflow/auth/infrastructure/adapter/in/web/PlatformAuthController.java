package com.styloflow.auth.infrastructure.adapter.in.web;

import com.styloflow.auth.application.port.in.AuthUseCase;
import com.styloflow.auth.infrastructure.adapter.in.web.dto.PlatformLoginRequest;
import com.styloflow.auth.infrastructure.adapter.in.web.dto.PlatformLoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/platform/auth")
@RequiredArgsConstructor
@Tag(name = "Platform", description = "Business administration (superadmin)")
public class PlatformAuthController {

    private final AuthUseCase authUseCase;
    private final AuthWebMapper authMapper;

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "POST /api/platform/auth/login — log in as the platform superadmin")
    public ResponseEntity<PlatformLoginResponse> login(@Valid @RequestBody PlatformLoginRequest request) {
        return ResponseEntity.ok(authMapper.toResponse(authUseCase.platformLogin(authMapper.toCommand(request))));
    }
}
