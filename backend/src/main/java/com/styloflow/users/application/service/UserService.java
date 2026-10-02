package com.styloflow.users.application.service;

import com.styloflow.shared.application.port.out.PasswordHasherPort;
import com.styloflow.shared.domain.exception.BusinessRuleException;
import com.styloflow.shared.domain.exception.NotFoundException;
import com.styloflow.users.application.port.in.UserCommand;
import com.styloflow.users.application.port.in.UserUseCase;
import com.styloflow.users.application.port.out.UserRepositoryPort;
import com.styloflow.users.domain.model.Role;
import com.styloflow.users.domain.model.User;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Manages business users and their passwords. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService implements UserUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordHasherPort passwordHasher;

    @Override
    public List<User> list() {
        return userRepository.findAllSorted();
    }

    @Override
    public List<User> stylists() {
        return userRepository.findActiveByRole(Role.STYLIST);
    }

    @Override
    public User get(Long id) {
        return getUserOrThrow(id);
    }

    @Override
    @Transactional
    public User create(UserCommand command) {
        if (command.password() == null || command.password().isBlank()) {
            throw new BusinessRuleException("A password is required when creating a user");
        }
        if (userRepository.existsByUsername(command.username())) {
            throw new BusinessRuleException("The username already exists");
        }
        User user = new User();
        apply(user, command);
        user.setPasswordHash(passwordHasher.hash(command.password()));
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User update(Long id, UserCommand command, Long currentUserId) {
        User user = getUserOrThrow(id);
        if (!user.getUsername().equalsIgnoreCase(command.username())
                && userRepository.existsByUsername(command.username())) {
            throw new BusinessRuleException("The username already exists");
        }
        boolean stopsBeingActiveAdmin = user.isActiveAdmin()
                && (command.role() != Role.ADMIN || Boolean.FALSE.equals(command.active()));
        if (stopsBeingActiveAdmin && userRepository.countActiveByRole(Role.ADMIN) <= 1) {
            throw new BusinessRuleException("There must be at least one active administrator");
        }
        if (id.equals(currentUserId) && Boolean.FALSE.equals(command.active())) {
            throw new BusinessRuleException("You cannot deactivate your own user");
        }
        apply(user, command);
        if (command.password() != null && !command.password().isBlank()) {
            user.setPasswordHash(passwordHasher.hash(command.password()));
        }
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(Long id, String password) {
        User user = getUserOrThrow(id);
        user.setPasswordHash(passwordHasher.hash(password));
        userRepository.save(user);
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User", id));
    }

    private static void apply(User user, UserCommand command) {
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
