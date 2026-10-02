package com.styloflow.catalog.application.port.out;

import com.styloflow.catalog.domain.model.Category;
import java.util.List;
import java.util.Optional;

/** Persistence of categories of the current business. */
public interface CategoryRepositoryPort {

    /** @return all categories sorted by name */
    List<Category> findAllSorted();

    /**
     * @param id category id
     * @return the category, or empty
     */
    Optional<Category> findById(Long id);

    /**
     * @param name exact category name
     * @return whether a category with that name exists
     */
    boolean existsByName(String name);

    /**
     * @param category category to persist
     * @return the persisted category
     */
    Category save(Category category);

    /** @param id category id */
    void deleteById(Long id);
}
