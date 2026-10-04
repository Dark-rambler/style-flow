package com.styloflow.catalog.application.port.in;

import com.styloflow.catalog.application.port.in.command.ProductCommand;
import com.styloflow.catalog.domain.model.ProductModel;

import java.util.List;

public interface ProductUseCase {

    List<ProductModel> list(boolean activeOnly);

    List<ProductModel> lowStock();

    ProductModel create(ProductCommand command);

    ProductModel update(Long id, ProductCommand command);

    ProductModel adjustStock(Long id, int quantity);
}
