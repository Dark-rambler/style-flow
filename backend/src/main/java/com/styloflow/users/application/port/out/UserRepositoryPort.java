package com.styloflow.users.application.port.out;

import com.styloflow.users.domain.enums.Role;
import com.styloflow.users.domain.model.UserModel;

import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {

    Optional<UserModel> findById(Long id);

    Optional<UserModel> findByUsernameInBusiness(long businessId, String username);

    boolean existsByUsername(String username);

    List<UserModel> findAllSorted();

    List<UserModel> findActiveByRole(Role role);

    long countActiveByRole(Role role);

    long count();

    UserModel save(UserModel user);
}
