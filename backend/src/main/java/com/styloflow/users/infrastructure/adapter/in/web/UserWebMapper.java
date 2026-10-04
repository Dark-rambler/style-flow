package com.styloflow.users.infrastructure.adapter.in.web;

import com.styloflow.users.application.port.in.command.UserCommand;
import com.styloflow.users.domain.model.UserModel;
import com.styloflow.users.infrastructure.adapter.in.web.dto.UserRequest;
import com.styloflow.users.infrastructure.adapter.in.web.dto.UserResponse;
import com.styloflow.users.infrastructure.adapter.in.web.dto.UserSummaryResponse;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserWebMapper {

    UserCommand toCommand(UserRequest request);

    UserResponse toResponse(UserModel user);

    List<UserResponse> toResponseList(List<UserModel> users);

    UserSummaryResponse toSummary(UserModel user);

    List<UserSummaryResponse> toSummaryList(List<UserModel> users);
}
