package com.styloflow.catalog.application.port.in;

import com.styloflow.catalog.domain.model.Category;
import java.util.List;

/** Management of service categories. */
public interface CategoryUseCase {

    /** @return all categories sorted by name */
    List<Category> list();

    /**
     * @param command category data
     * @return the created category
     * @throws com.styloflow.shared.domain.exception.BusinessRuleException if the name already exists
     */
    Category create(CategoryCommand command);

    /**
     * @param id category id
     * @param command new data
     * @return the updated category
     * @throws com.styloflow.shared.domain.exception.NotFoundException if it does not exist
     */
    Category update(Long id, CategoryCommand command);

    /**
     * Deletes a category without services.
     *
     * @param id category id
     * @throws com.styloflow.shared.domain.exception.BusinessRuleException if it still has services
     */
    void delete(Long id);
}
