package com.styloflow.catalog.application.port.out;

import com.styloflow.catalog.domain.model.ProductModel;
import java.util.List;
import java.util.Optional;

public interface ProductRepositoryPort {

    List<ProductModel> findAllSorted();

    List<ProductModel> findLowStock();

    Optional<ProductModel> findById(Long id);

    Optional<ProductModel> findByIdUpdate(Long id);

    boolean existsBySku(String sku);

    ProductModel save(ProductModel product);
}
