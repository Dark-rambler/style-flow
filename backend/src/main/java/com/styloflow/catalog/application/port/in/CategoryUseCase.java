package com.styloflow.catalog.application.port.in;

import com.styloflow.catalog.application.port.in.command.CategoryCommand;
import com.styloflow.catalog.domain.model.CategoryModel;

import java.util.List;

public interface CategoryUseCase {

    List<CategoryModel> list();

    CategoryModel create(CategoryCommand command);

    CategoryModel update(Long id, CategoryCommand command);

    void delete(Long id);
}
