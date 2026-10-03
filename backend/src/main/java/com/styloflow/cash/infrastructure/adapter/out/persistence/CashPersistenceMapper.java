package com.styloflow.cash.infrastructure.adapter.out.persistence;

import com.styloflow.cash.domain.model.Cash;
import com.styloflow.users.infrastructure.adapter.out.persistence.UserPersistenceMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = UserPersistenceMapper.class)
public interface CashPersistenceMapper {

    Cash toDomain(CashEntity entity);

    @Mapping(target = "openedBy", ignore = true)
    @Mapping(target = "closedBy", ignore = true)
    void updateEntity(Cash cash, @MappingTarget CashEntity entity);
}
