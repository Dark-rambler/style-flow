package com.styloflow.cashregister.infrastructure.adapter.out.persistence;

import com.styloflow.cashregister.domain.model.CashRegister;
import com.styloflow.users.infrastructure.adapter.out.persistence.UserPersistenceMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = UserPersistenceMapper.class)
public interface CashRegisterPersistenceMapper {

    CashRegister toDomain(CashRegisterEntity entity);

    /** Users are set as references by the adapter. */
    @Mapping(target = "openedBy", ignore = true)
    @Mapping(target = "closedBy", ignore = true)
    void updateEntity(CashRegister cashRegister, @MappingTarget CashRegisterEntity entity);
}
