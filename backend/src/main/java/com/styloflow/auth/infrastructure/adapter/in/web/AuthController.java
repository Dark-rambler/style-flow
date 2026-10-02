package com.styloflow.auth.infrastructure.adapter.in.web;

import com.styloflow.auth.application.port.in.AuthUseCase;
import com.styloflow.auth.infrastructure.adapter.in.web.dto.LoginRequest;
import com.styloflow.auth.infrastructure.adapter.in.web.dto.LoginResponse;
import com.styloflow.auth.infrastructure.security.CurrentUser;
import com.styloflow.users.infrastructure.adapter.in.web.UserWebMapper;
import com.styloflow.users.infrastructure.adapter.in.web.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Login of business users")
public class AuthController {

    private final AuthUseCase authUseCase;
    private final AuthWebMapper authMapper;
    private final UserWebMapper userMapper;
    private final CurrentUser currentUser;

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "POST /api/auth/login — log in with business code, username and password")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authMapper.toResponse(authUseCase.login(authMapper.toCommand(request))));
    }

    @GetMapping("/me")
    @Operation(summary = "GET /api/auth/me — authenticated user")
    public ResponseEntity<UserResponse> me() {
        return ResponseEntity.ok(userMapper.toResponse(authUseCase.currentUser(currentUser.id())));
    }
}
