package com.styloflow.catalog.application.port.in;

import com.styloflow.catalog.application.port.in.command.ProductCommand;
import com.styloflow.catalog.domain.model.Product;
import java.util.List;

public interface ProductUseCase {

    List<Product> list(boolean activeOnly);

    List<Product> lowStock();

    Product create(ProductCommand command);

    Product update(Long id, ProductCommand command);

    Product adjustStock(Long id, int quantity);
}
