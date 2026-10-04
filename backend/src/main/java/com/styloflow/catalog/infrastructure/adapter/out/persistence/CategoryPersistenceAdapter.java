package com.styloflow.catalog.infrastructure.adapter.out.persistence;

import com.styloflow.catalog.application.port.out.CategoryRepositoryPort;
import com.styloflow.catalog.domain.model.CategoryModel;
import com.styloflow.shared.domain.exception.NotFoundException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryPersistenceAdapter implements CategoryRepositoryPort {

    private final CategoryJpaRepository categoryRepository;
    private final CatalogPersistenceMapper catalogMapper;

    @Override
    public List<CategoryModel> findAllSorted() {
        return catalogMapper.toCategoryList(categoryRepository.findAllByOrderByNameAsc());
    }

    @Override
    public Optional<CategoryModel> findById(Long id) {
        return categoryRepository.findById(id).map(catalogMapper::toDomain);
    }

    @Override
    public boolean existsByName(String name) {
        return categoryRepository.existsByNameIgnoreCase(name);
    }

    @Override
    public CategoryModel save(CategoryModel category) {
        var entity = category.getId() == null ? new CategoryEntity() :
                categoryRepository.findById(category.getId())
                        .orElseThrow(() -> new NotFoundException("Category", category.getId()));
        catalogMapper.updateEntity(category, entity);
        return catalogMapper.toDomain(categoryRepository.save(entity));
    }

    @Override
    public void deleteById(Long id) {
        categoryRepository.deleteById(id);
    }
}
