package com.styloflow.users.application.port.in;

import com.styloflow.users.domain.model.User;
import java.util.List;

public interface UserUseCase {

    /** @return all users of the business sorted by name */
    List<User> list();

    /** Active stylists, to assign them to services in the POS. */
    List<User> stylists();

    /**
     * @param id user id
     * @return the user
     * @throws com.styloflow.shared.domain.exception.NotFoundException if it does not exist
     */
    User get(Long id);

    /**
     * Creates a user hashing its password.
     *
     * @param command user data (password required)
     * @return the created user
     * @throws com.styloflow.shared.domain.exception.BusinessRuleException if the username exists or the password is missing
     */
    User create(UserCommand command);

    /**
     * Updates a user; keeps at least one active administrator and forbids self-deactivation.
     *
     * @param id user id
     * @param command new data (blank password keeps the current one)
     * @param currentUserId user making the change
     * @return the updated user
     */
    User update(Long id, UserCommand command, Long currentUserId);

    /**
     * @param id user id
     * @param password new plain-text password
     */
    void changePassword(Long id, String password);
}
