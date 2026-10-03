package com.styloflow.catalog.infrastructure.adapter.out.persistence;

import com.styloflow.catalog.domain.model.Category;
import com.styloflow.catalog.domain.model.Product;
import com.styloflow.catalog.domain.model.Service;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CatalogPersistenceMapper {

    Category toDomain(CategoryEntity entity);

    List<Category> toCategoryList(List<CategoryEntity> entities);

    void updateEntity(Category category, @MappingTarget CategoryEntity entity);

    Service toDomain(ServiceEntity entity);

    List<Service> toServiceList(List<ServiceEntity> entities);

    @Mapping(target = "category", ignore = true)
    void updateEntity(Service service, @MappingTarget ServiceEntity entity);

    Product toDomain(ProductEntity entity);

    List<Product> toProductList(List<ProductEntity> entities);

    void updateEntity(Product product, @MappingTarget ProductEntity entity);
}
