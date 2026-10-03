package com.styloflow.users.infrastructure.adapter.in.web;

import com.styloflow.users.application.port.in.UserUseCase;
import com.styloflow.users.infrastructure.adapter.in.web.dto.ChangePasswordRequest;
import com.styloflow.users.infrastructure.adapter.in.web.dto.UserRequest;
import com.styloflow.users.infrastructure.adapter.in.web.dto.UserResponse;
import com.styloflow.users.infrastructure.adapter.in.web.dto.UserSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Users of the business")
public class UserController {

    private final UserUseCase userUseCase;
    private final UserWebMapper userMapper;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "GET /api/users — list the users of the business")
    public ResponseEntity<List<UserResponse>> list() {
        return ResponseEntity.ok(userMapper.toResponseList(userUseCase.list()));
    }

    @GetMapping("/stylists")
    @Operation(summary = "GET /api/users/stylists — active stylists, to assign them to services in the POS")
    public ResponseEntity<List<UserSummaryResponse>> stylists() {
        return ResponseEntity.ok(userMapper.toSummaryList(userUseCase.stylists()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "GET /api/users/{id} — get a user by id")
    public ResponseEntity<UserResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(userMapper.toResponse(userUseCase.get(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "POST /api/users — create a user")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userMapper.toResponse(userUseCase.create(userMapper.toCommand(request))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "PUT /api/users/{id} — update a user")
    public ResponseEntity<UserResponse> update(@PathVariable Long id, @Valid @RequestBody UserRequest request, Authentication auth) {
        return ResponseEntity.ok(userMapper.toResponse(
                userUseCase.update(id, userMapper.toCommand(request), Long.parseLong(auth.getName()))));
    }

    @PutMapping("/me/password")
    @Operation(summary = "PUT /api/users/me/password — change the password of the authenticated user")
    public ResponseEntity<Void> changeMyPassword(@Valid @RequestBody ChangePasswordRequest request, Authentication auth) {
        userUseCase.changePassword(Long.parseLong(auth.getName()), request.password());
        return ResponseEntity.noContent().build();
    }
}
