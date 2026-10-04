package com.styloflow.catalog.infrastructure.adapter.in.web;

import com.styloflow.catalog.application.port.in.command.CategoryCommand;
import com.styloflow.catalog.application.port.in.command.ProductCommand;
import com.styloflow.catalog.application.port.in.command.ServiceCommand;
import com.styloflow.catalog.domain.model.CategoryModel;
import com.styloflow.catalog.domain.model.ProductModel;
import com.styloflow.catalog.domain.model.ServiceModel;
import com.styloflow.catalog.infrastructure.adapter.in.web.dto.*;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CatalogWebMapper {

    CategoryCommand toCommand(CategoryRequest request);

    CategoryResponse toResponse(CategoryModel category);

    List<CategoryResponse> toCategoryResponseList(List<CategoryModel> categories);

    ServiceCommand toCommand(ServiceRequest request);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "category", source = "category.name")
    ServiceResponse toResponse(ServiceModel service);

    List<ServiceResponse> toServiceResponseList(List<ServiceModel> services);

    ProductCommand toCommand(ProductRequest request);

    ProductResponse toResponse(ProductModel product);

    List<ProductResponse> toProductResponseList(List<ProductModel> products);
}
