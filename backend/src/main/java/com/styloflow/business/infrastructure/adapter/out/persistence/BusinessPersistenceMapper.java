package com.styloflow.business.infrastructure.adapter.out.persistence;

import com.styloflow.business.domain.model.Business;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BusinessPersistenceMapper {

    Business toDomain(BusinessEntity entity);

    @Mapping(target = "code", ignore = true)
    void updateEntity(Business business, @MappingTarget BusinessEntity entity);
}
