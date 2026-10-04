package com.styloflow.platform.infrastructure.adapter.out.persistence;

import com.styloflow.platform.domain.model.SuperadminModel;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SuperadminPersistenceMapper {

    SuperadminModel toDomain(SuperadminEntity entity);

    void updateEntity(SuperadminModel superadmin, @MappingTarget SuperadminEntity entity);
}
