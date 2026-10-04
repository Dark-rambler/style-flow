package com.styloflow.users.application.port.in;

import com.styloflow.users.application.port.in.command.UserCommand;
import com.styloflow.users.domain.model.UserModel;

import java.util.List;

public interface UserUseCase {

    List<UserModel> list();

    List<UserModel> stylists();

    UserModel get(Long id);

    UserModel create(UserCommand command);

    UserModel update(Long id, UserCommand command, Long currentUserId);

    void changePassword(Long id, String password);
}
