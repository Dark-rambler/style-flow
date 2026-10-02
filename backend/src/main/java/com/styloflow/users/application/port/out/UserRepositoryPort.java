package com.styloflow.users.application.port.out;

import com.styloflow.users.domain.model.Role;
import com.styloflow.users.domain.model.User;
import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {

    /**
     * @param id user id
     * @return the user, or empty
     */
    Optional<User> findById(Long id);

    /** Looks the user up inside a given business, regardless of the request tenant (login). */
    Optional<User> findByUsernameInBusiness(long businessId, String username);

    /**
     * @param username login name
     * @return whether it is taken
     */
    boolean existsByUsername(String username);

    /** @return all users sorted by name */
    List<User> findAllSorted();

    /**
     * @param role role to filter by
     * @return active users with that role
     */
    List<User> findActiveByRole(Role role);

    /**
     * @param role role to filter by
     * @return number of active users with that role
     */
    long countActiveByRole(Role role);

    /** @return number of users */
    long count();

    /**
     * @param user user to persist
     * @return the persisted user
     */
    User save(User user);
}
