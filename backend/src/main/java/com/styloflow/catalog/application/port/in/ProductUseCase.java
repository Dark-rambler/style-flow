package com.styloflow.catalog.application.port.in;

import com.styloflow.catalog.domain.model.Product;
import java.util.List;

/** Management of retail products and their stock. */
public interface ProductUseCase {

    /**
     * @param activeOnly whether to skip inactive products
     * @return the products sorted by name
     */
    List<Product> list(boolean activeOnly);

    /** @return active products at or below their minimum stock */
    List<Product> lowStock();

    /**
     * @param command product data
     * @return the created product
     * @throws com.styloflow.shared.domain.exception.BusinessRuleException if the SKU already exists
     */
    Product create(ProductCommand command);

    /**
     * @param id product id
     * @param command new data
     * @return the updated product
     * @throws com.styloflow.shared.domain.exception.NotFoundException if it does not exist
     */
    Product update(Long id, ProductCommand command);

    /**
     * Adds or removes units (purchases, shrinkage, inventory counts) under a pessimistic lock.
     *
     * @param id product id
     * @param quantity units to add, negative to remove
     * @return the updated product
     * @throws com.styloflow.shared.domain.exception.BusinessRuleException if the stock would become negative
     */
    Product adjustStock(Long id, int quantity);
}
