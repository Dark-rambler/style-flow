package com.styloflow.users.application.port.in;

import com.styloflow.users.application.port.in.command.UserCommand;
import com.styloflow.users.domain.model.User;
import java.util.List;

public interface UserUseCase {

    List<User> list();

    List<User> stylists();

    User get(Long id);

    User create(UserCommand command);

    User update(Long id, UserCommand command, Long currentUserId);

    void changePassword(Long id, String password);
}
