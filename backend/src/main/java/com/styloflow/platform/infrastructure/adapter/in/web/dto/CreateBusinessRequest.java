package com.styloflow.platform.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateBusinessRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Pattern(regexp = "^[a-z0-9-]{3,40}$",
                message = "3 to 40 characters: lowercase letters, numbers and hyphens") String code,
        @Size(max = 30) String taxId,
        @Size(max = 30) String phone,
        @NotBlank @Size(max = 120) String adminName,
        @NotBlank @Size(min = 3, max = 50) @Pattern(regexp = "^[a-zA-Z0-9._-]+$",
                message = "only letters, numbers, dot, hyphen and underscore") String adminUsername,
        @NotBlank @Size(min = 6, max = 72) String adminPassword,
        boolean baseCatalog
) {}
