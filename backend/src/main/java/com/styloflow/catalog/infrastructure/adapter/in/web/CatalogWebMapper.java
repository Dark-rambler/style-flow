package com.styloflow.catalog.infrastructure.adapter.in.web;

import com.styloflow.catalog.application.port.in.CategoryCommand;
import com.styloflow.catalog.application.port.in.ProductCommand;
import com.styloflow.catalog.application.port.in.SalonServiceCommand;
import com.styloflow.catalog.domain.model.Category;
import com.styloflow.catalog.domain.model.Product;
import com.styloflow.catalog.domain.model.SalonService;
import com.styloflow.catalog.infrastructure.adapter.in.web.dto.CategoryRequest;
import com.styloflow.catalog.infrastructure.adapter.in.web.dto.CategoryResponse;
import com.styloflow.catalog.infrastructure.adapter.in.web.dto.ProductRequest;
import com.styloflow.catalog.infrastructure.adapter.in.web.dto.ProductResponse;
import com.styloflow.catalog.infrastructure.adapter.in.web.dto.SalonServiceRequest;
import com.styloflow.catalog.infrastructure.adapter.in.web.dto.SalonServiceResponse;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CatalogWebMapper {

    CategoryCommand toCommand(CategoryRequest request);

    CategoryResponse toResponse(Category category);

    List<CategoryResponse> toCategoryResponseList(List<Category> categories);

    SalonServiceCommand toCommand(SalonServiceRequest request);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "category", source = "category.name")
    SalonServiceResponse toResponse(SalonService service);

    List<SalonServiceResponse> toServiceResponseList(List<SalonService> services);

    ProductCommand toCommand(ProductRequest request);

    ProductResponse toResponse(Product product);

    List<ProductResponse> toProductResponseList(List<Product> products);
}
