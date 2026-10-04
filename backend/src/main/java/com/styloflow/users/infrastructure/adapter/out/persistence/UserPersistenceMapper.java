package com.styloflow.users.infrastructure.adapter.out.persistence;

import com.styloflow.users.domain.model.UserModel;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserPersistenceMapper {

    UserModel toDomain(UserEntity entity);

    List<UserModel> toDomainList(List<UserEntity> entities);

    void updateEntity(UserModel user, @MappingTarget UserEntity entity);
}
