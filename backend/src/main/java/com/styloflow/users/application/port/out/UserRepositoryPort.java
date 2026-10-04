package com.styloflow.users.application.port.out;

import com.styloflow.users.domain.enums.Role;
import com.styloflow.users.domain.model.User;
import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {

    Optional<User> findById(Long id);

    Optional<User> findByUsernameInBusiness(long businessId, String username);

    boolean existsByUsername(String username);

    List<User> findAllSorted();

    List<User> findActiveByRole(Role role);

    long countActiveByRole(Role role);

    long count();

    User save(User user);
}
