package com.styloflow.catalog.application.port.out;

import com.styloflow.catalog.domain.model.CategoryModel;

import java.util.List;
import java.util.Optional;

public interface CategoryRepositoryPort {

    List<CategoryModel> findAllSorted();

    Optional<CategoryModel> findById(Long id);

    boolean existsByName(String name);

    CategoryModel save(CategoryModel category);

    void deleteById(Long id);
}
