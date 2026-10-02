package com.styloflow.catalog.application.port.out;

import com.styloflow.catalog.domain.model.Product;
import java.util.List;
import java.util.Optional;

public interface ProductRepositoryPort {

    /** @return all products sorted by name */
    List<Product> findAllSorted();

    /** Active products with stock at or below the minimum. */
    List<Product> findLowStock();

    /**
     * @param id product id
     * @return the product, or empty
     */
    Optional<Product> findById(Long id);

    /** Locks the row until the end of the transaction to change stock without race conditions. */
    Optional<Product> findByIdForUpdate(Long id);

    /**
     * @param sku product SKU
     * @return whether a product with that SKU exists
     */
    boolean existsBySku(String sku);

    /**
     * @param product product to persist
     * @return the persisted product
     */
    Product save(Product product);
}
