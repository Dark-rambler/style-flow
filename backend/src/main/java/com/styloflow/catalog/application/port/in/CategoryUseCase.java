package com.styloflow.catalog.application.port.in;

import com.styloflow.catalog.application.port.in.command.CategoryCommand;
import com.styloflow.catalog.domain.model.Category;
import java.util.List;

public interface CategoryUseCase {

    List<Category> list();

    Category create(CategoryCommand command);

    Category update(Long id, CategoryCommand command);

    void delete(Long id);
}
