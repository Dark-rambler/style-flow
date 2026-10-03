package com.styloflow.platform.infrastructure.config;

import com.styloflow.business.application.port.out.BusinessRepositoryPort;
import com.styloflow.platform.application.port.out.SuperadminRepositoryPort;
import com.styloflow.platform.domain.model.Superadmin;
import com.styloflow.shared.application.port.out.PasswordHasherPort;
import com.styloflow.shared.infrastructure.config.AppProperties;
import com.styloflow.shared.infrastructure.tenant.TenantExecutor;
import com.styloflow.users.application.port.out.UserRepositoryPort;
import com.styloflow.users.domain.enums.Role;
import com.styloflow.users.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Minimum data to start: the platform superadmin and, if the "demo" business has no users, its administrator
 * (useful in development).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlatformInitializer implements ApplicationRunner {

    static final String DEMO_BUSINESS = "demo";

    private final SuperadminRepositoryPort superadminRepository;
    private final BusinessRepositoryPort businessRepository;
    private final UserRepositoryPort userRepository;
    private final PasswordHasherPort passwordHasher;
    private final TenantExecutor tenantExecutor;
    private final AppProperties props;

    @Override
    public void run(@NonNull ApplicationArguments args) {
        if (superadminRepository.count() == 0) {
            Superadmin superadmin = superadminRepository.save(Superadmin.builder()
                    .name(props.superadmin().name())
                    .username(props.superadmin().username())
                    .passwordHash(passwordHasher.hash(props.superadmin().password()))
                    .build());
            log.warn("Platform superadmin created: '{}'. Change its password (SUPERADMIN_PASSWORD).",
                    superadmin.getUsername());
        }

        businessRepository.findByCode(DEMO_BUSINESS).ifPresent(demo -> tenantExecutor.runAs(demo.getId(), () -> {
            if (userRepository.count() == 0) {
                User admin = userRepository.save(User.builder()
                        .name(props.admin().name())
                        .username(props.admin().username())
                        .passwordHash(passwordHasher.hash(props.admin().password()))
                        .role(Role.ADMIN)
                        .build());
                log.warn("Administrator of business '{}' created: '{}'.", DEMO_BUSINESS, admin.getUsername());
            }
            return null;
        }));
    }
}
