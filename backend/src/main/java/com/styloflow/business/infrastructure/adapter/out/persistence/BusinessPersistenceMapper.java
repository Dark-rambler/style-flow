package com.styloflow.business.infrastructure.adapter.out.persistence;

import com.styloflow.business.domain.model.BusinessModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BusinessPersistenceMapper {

    BusinessModel toDomain(BusinessEntity entity);

    @Mapping(target = "code", ignore = true)
    void updateEntity(BusinessModel business, @MappingTarget BusinessEntity entity);
}
