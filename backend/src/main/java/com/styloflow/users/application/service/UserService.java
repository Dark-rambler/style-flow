package com.styloflow.users.application.service;

import com.styloflow.shared.application.port.out.PasswordHasherPort;
import com.styloflow.shared.domain.exception.BusinessRuleException;
import com.styloflow.shared.domain.exception.NotFoundException;
import com.styloflow.users.application.port.in.command.UserCommand;
import com.styloflow.users.application.port.in.UserUseCase;
import com.styloflow.users.application.port.out.UserRepositoryPort;
import com.styloflow.users.domain.enums.Role;
import com.styloflow.users.domain.model.UserModel;
import java.math.BigDecimal;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService implements UserUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordHasherPort passwordHasher;

    @Override
    public List<UserModel> list() {
        return userRepository.findAllSorted();
    }

    @Override
    public List<UserModel> stylists() {
        return userRepository.findActiveByRole(Role.STYLIST);
    }

    @Override
    public UserModel get(Long id) {
        return getUserOrThrow(id);
    }

    @Override
    @Transactional
    public UserModel create(UserCommand command) {
        if (command.password() == null || command.password().isBlank())
            throw new BusinessRuleException("A password is required when creating a user");
        if (userRepository.existsByUsername(command.username()))
            throw new BusinessRuleException("The username already exists");
        UserModel user = new UserModel();
        apply(user, command);
        user.setPasswordHash(passwordHasher.hash(command.password()));
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public UserModel update(Long id, UserCommand command, Long currentUserId) {
        UserModel user = getUserOrThrow(id);
        if (!user.getUsername().equals(command.username()) && userRepository.existsByUsername(command.username()))
            throw new BusinessRuleException("The username already exists");
        if (userRepository.countActiveByRole(Role.ADMIN) < 1)
            throw new BusinessRuleException("There must be at least one active administrator");
        if (id.equals(currentUserId) && !command.active())
            throw new BusinessRuleException("You cannot deactivate your own user");
        if (id.equals(currentUserId) && Role.ADMIN.equals(user.getRole()) && !command.role().equals(user.getRole()))
            throw new BusinessRuleException("You cannot change your own role");
        apply(user, command);
        if (command.password() != null && !command.password().isBlank())
            user.setPasswordHash(passwordHasher.hash(command.password()));
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(Long id, String password) {
        UserModel user = getUserOrThrow(id);
        user.setPasswordHash(passwordHasher.hash(password));
        userRepository.save(user);
    }

    private UserModel getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User", id));
    }

    private static void apply(UserModel user, UserCommand command) {
        user.setName(command.name().trim());
        user.setUsername(command.username().trim().toLowerCase());
        user.setRole(command.role());
        user.setPhone(command.phone());
        user.setCommissionRate(command.commissionRate() != null ? command.commissionRate() : BigDecimal.ZERO);
        if (command.active() != null) {
            user.setActive(command.active());
        }
    }
}
