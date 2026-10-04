package com.styloflow.catalog.application.port.out;

import com.styloflow.catalog.domain.model.Category;
import java.util.List;
import java.util.Optional;

public interface CategoryRepositoryPort {

    List<Category> findAllSorted();

    Optional<Category> findById(Long id);

    boolean existsByName(String name);

    Category save(Category category);

    void deleteById(Long id);
}
