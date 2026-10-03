package com.styloflow.catalog.application.port.out;

import com.styloflow.catalog.domain.model.Product;
import java.util.List;
import java.util.Optional;

public interface ProductRepositoryPort {

    List<Product> findAllSorted(boolean activeOnly);

    List<Product> findLowStock();

    Optional<Product> findById(Long id);

    Optional<Product> findByIdForUpdate(Long id);

    boolean existsBySku(String sku);

    Product save(Product product);
}
