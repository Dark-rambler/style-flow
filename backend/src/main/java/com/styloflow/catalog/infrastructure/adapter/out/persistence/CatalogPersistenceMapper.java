package com.styloflow.catalog.infrastructure.adapter.out.persistence;

import com.styloflow.catalog.domain.model.CategoryModel;
import com.styloflow.catalog.domain.model.ProductModel;
import com.styloflow.catalog.domain.model.ServiceModel;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CatalogPersistenceMapper {

    CategoryModel toDomain(CategoryEntity entity);

    List<CategoryModel> toCategoryList(List<CategoryEntity> entities);

    void updateEntity(CategoryModel category, @MappingTarget CategoryEntity entity);

    ServiceModel toDomain(ServiceEntity entity);

    List<ServiceModel> toServiceList(List<ServiceEntity> entities);

    @Mapping(target = "category", ignore = true)
    void updateEntity(ServiceModel service, @MappingTarget ServiceEntity entity);

    ProductModel toDomain(ProductEntity entity);

    List<ProductModel> toProductList(List<ProductEntity> entities);

    void updateEntity(ProductModel product, @MappingTarget ProductEntity entity);
}
