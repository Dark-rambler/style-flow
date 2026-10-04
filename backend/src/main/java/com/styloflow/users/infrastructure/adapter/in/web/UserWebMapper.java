package com.styloflow.users.infrastructure.adapter.in.web;

import com.styloflow.users.application.port.in.command.UserCommand;
import com.styloflow.users.domain.model.User;
import com.styloflow.users.infrastructure.adapter.in.web.dto.UserRequest;
import com.styloflow.users.infrastructure.adapter.in.web.dto.UserResponse;
import com.styloflow.users.infrastructure.adapter.in.web.dto.UserSummaryResponse;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserWebMapper {

    UserCommand toCommand(UserRequest request);

    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);

    UserSummaryResponse toSummary(User user);

    List<UserSummaryResponse> toSummaryList(List<User> users);
}
