package com.styloflow.catalog.application.service;

import com.styloflow.catalog.application.port.in.CategoryCommand;
import com.styloflow.catalog.application.port.in.CategoryUseCase;
import com.styloflow.catalog.application.port.out.CategoryRepositoryPort;
import com.styloflow.catalog.application.port.out.SalonServiceRepositoryPort;
import com.styloflow.catalog.domain.model.Category;
import com.styloflow.shared.domain.exception.BusinessRuleException;
import com.styloflow.shared.domain.exception.NotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Default {@link CategoryUseCase} implementation. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService implements CategoryUseCase {

    private final CategoryRepositoryPort categoryRepository;
    private final SalonServiceRepositoryPort serviceRepository;

    @Override
    public List<Category> list() {
        return categoryRepository.findAllSorted();
    }

    @Override
    @Transactional
    public Category create(CategoryCommand command) {
        if (categoryRepository.existsByName(command.name().trim())) {
            throw new BusinessRuleException("A category with that name already exists");
        }
        Category category = new Category();
        apply(category, command);
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public Category update(Long id, CategoryCommand command) {
        Category category = getCategoryOrThrow(id);
        apply(category, command);
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (serviceRepository.countByCategory(id) > 0) {
            throw new BusinessRuleException("The category has services; deactivate it instead of deleting it");
        }
        categoryRepository.deleteById(getCategoryOrThrow(id).getId());
    }

    private Category getCategoryOrThrow(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new NotFoundException("Category", id));
    }

    private static void apply(Category category, CategoryCommand command) {
        category.setName(command.name().trim());
        if (command.active() != null) {
            category.setActive(command.active());
        }
    }
}
