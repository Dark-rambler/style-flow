package com.styloflow.users.infrastructure.adapter.in.web.dto;

import com.styloflow.users.domain.model.Role;

/** Reduced view for pickers (e.g. choosing a stylist in the POS). */
public record UserSummaryResponse(Long id, String name, Role role) {}
