package com.styloflow.platform.infrastructure.adapter.out.persistence;

import com.styloflow.platform.domain.model.Superadmin;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SuperadminPersistenceMapper {

    Superadmin toDomain(SuperadminEntity entity);

    void updateEntity(Superadmin superadmin, @MappingTarget SuperadminEntity entity);
}
