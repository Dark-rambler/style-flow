package com.styloflow.auth.application.service;

import com.styloflow.auth.application.port.in.AuthUseCase;
import com.styloflow.auth.application.port.in.command.LoginCommand;
import com.styloflow.auth.application.port.in.command.PlatformLoginCommand;
import com.styloflow.auth.application.port.out.TokenPort;
import com.styloflow.auth.domain.model.BusinessSession;
import com.styloflow.auth.domain.model.PlatformSession;
import com.styloflow.business.application.port.out.BusinessRepositoryPort;
import com.styloflow.platform.application.port.out.SuperadminRepositoryPort;
import com.styloflow.platform.domain.model.Superadmin;
import com.styloflow.shared.application.port.out.PasswordHasherPort;
import com.styloflow.shared.domain.exception.ForbiddenException;
import com.styloflow.shared.domain.exception.UnauthorizedException;
import com.styloflow.users.application.port.out.UserRepositoryPort;
import com.styloflow.users.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService implements AuthUseCase {

    private final BusinessRepositoryPort businessRepository;
    private final UserRepositoryPort userRepository;
    private final SuperadminRepositoryPort superadminRepository;
    private final PasswordHasherPort passwordHasher;
    private final TokenPort tokenPort;

    @Override
    public BusinessSession login(LoginCommand command) {
        var invalidCredentials = new UnauthorizedException("Invalid business, username or password");
        var business = businessRepository.findByCode(command.businessCode().trim())
                .orElseThrow(() -> invalidCredentials);
        if (!business.isActive())
            throw new ForbiddenException("This business is suspended. Please contact support.");
        var user = userRepository.findByUsernameInBusiness(business.getId(), command.username().trim())
                .filter(User::isActive)
                .filter(u -> passwordHasher.matches(command.password(), u.getPasswordHash()))
                .orElseThrow(() -> invalidCredentials);
        return new BusinessSession(tokenPort.generate(user, business), user, business);
    }

    @Override
    @Transactional(readOnly = true)
    public User currentUser(Long userId) {
        return userRepository.findById(userId)
                .filter(User::isActive)
                .orElseThrow(() -> new UnauthorizedException("The session is no longer valid"));
    }

    @Override
    public PlatformSession platformLogin(PlatformLoginCommand command) {
        Superadmin superadmin = superadminRepository.findByUsername(command.username().trim())
                .filter(Superadmin::isActive)
                .filter(s -> passwordHasher.matches(command.password(), s.getPasswordHash()))
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));
        return new PlatformSession(tokenPort.generatePlatform(superadmin), superadmin);
    }
}
