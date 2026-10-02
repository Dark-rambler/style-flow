package com.styloflow.users.infrastructure.adapter.out.persistence;

import com.styloflow.users.domain.model.User;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserPersistenceMapper {

    User toDomain(UserEntity entity);

    List<User> toDomainList(List<UserEntity> entities);

    void updateEntity(User user, @MappingTarget UserEntity entity);
}
