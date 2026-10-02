package com.styloflow.platform.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Platform user: creates and suspends businesses. Does not belong to any business. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Superadmin {

    private Long id;
    private String name;
    private String username;
    private String passwordHash;
    @Builder.Default
    private boolean active = true;
}
