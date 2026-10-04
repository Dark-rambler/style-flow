package com.styloflow.auth.infrastructure.adapter.in.web;

import com.styloflow.auth.application.port.in.command.LoginCommand;
import com.styloflow.auth.application.port.in.command.PlatformLoginCommand;
import com.styloflow.auth.domain.model.BusinessSession;
import com.styloflow.auth.domain.model.PlatformSession;
import com.styloflow.auth.infrastructure.adapter.in.web.dto.BusinessInfo;
import com.styloflow.auth.infrastructure.adapter.in.web.dto.LoginRequest;
import com.styloflow.auth.infrastructure.adapter.in.web.dto.LoginResponse;
import com.styloflow.auth.infrastructure.adapter.in.web.dto.PlatformLoginRequest;
import com.styloflow.auth.infrastructure.adapter.in.web.dto.PlatformLoginResponse;
import com.styloflow.business.domain.model.BusinessModel;
import com.styloflow.users.infrastructure.adapter.in.web.UserWebMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = UserWebMapper.class)
public interface AuthWebMapper {

    LoginCommand toCommand(LoginRequest request);

    PlatformLoginCommand toCommand(PlatformLoginRequest request);

    @Mapping(target = "token", source = "token.value")
    @Mapping(target = "expiresAt", source = "token.expiresAt")
    LoginResponse toResponse(BusinessSession session);

    BusinessInfo toBusinessInfo(BusinessModel business);

    @Mapping(target = "token", source = "token.value")
    @Mapping(target = "expiresAt", source = "token.expiresAt")
    @Mapping(target = "name", source = "superadmin.name")
    PlatformLoginResponse toResponse(PlatformSession session);
}
