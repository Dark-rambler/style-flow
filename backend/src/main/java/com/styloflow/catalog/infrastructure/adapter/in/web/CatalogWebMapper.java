package com.styloflow.catalog.infrastructure.adapter.in.web;

import com.styloflow.catalog.application.port.in.command.CategoryCommand;
import com.styloflow.catalog.application.port.in.command.ProductCommand;
import com.styloflow.catalog.application.port.in.command.ServiceCommand;
import com.styloflow.catalog.domain.model.Category;
import com.styloflow.catalog.domain.model.Product;
import com.styloflow.catalog.domain.model.Service;
import com.styloflow.catalog.infrastructure.adapter.in.web.dto.*;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CatalogWebMapper {

    CategoryCommand toCommand(CategoryRequest request);

    CategoryResponse toResponse(Category category);

    List<CategoryResponse> toCategoryResponseList(List<Category> categories);

    ServiceCommand toCommand(ServiceRequest request);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "category", source = "category.name")
    ServiceResponse toResponse(Service service);

    List<ServiceResponse> toServiceResponseList(List<Service> services);

    ProductCommand toCommand(ProductRequest request);

    ProductResponse toResponse(Product product);

    List<ProductResponse> toProductResponseList(List<Product> products);
}
