package com.styloflow.catalog.infrastructure.adapter.out.persistence;

import com.styloflow.catalog.domain.model.Category;
import com.styloflow.catalog.domain.model.Product;
import com.styloflow.catalog.domain.model.SalonService;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CatalogPersistenceMapper {

    Category toDomain(CategoryEntity entity);

    List<Category> toCategoryList(List<CategoryEntity> entities);

    void updateEntity(Category category, @MappingTarget CategoryEntity entity);

    SalonService toDomain(SalonServiceEntity entity);

    List<SalonService> toServiceList(List<SalonServiceEntity> entities);

    /** The category is set as a reference by the adapter. */
    @Mapping(target = "category", ignore = true)
    void updateEntity(SalonService service, @MappingTarget SalonServiceEntity entity);

    Product toDomain(ProductEntity entity);

    List<Product> toProductList(List<ProductEntity> entities);

    void updateEntity(Product product, @MappingTarget ProductEntity entity);
}
