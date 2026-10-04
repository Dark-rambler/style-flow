package com.styloflow.catalog.infrastructure.adapter.out.persistence;

import com.styloflow.catalog.application.port.out.ProductRepositoryPort;
import com.styloflow.catalog.domain.model.Product;
import com.styloflow.shared.domain.exception.NotFoundException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductPersistenceAdapter implements ProductRepositoryPort {

    private final ProductJpaRepository productRepository;
    private final CatalogPersistenceMapper catalogMapper;

    @Override
    public List<Product> findAllSorted() {
        return catalogMapper.toProductList(productRepository.findAllByOrderByNameAsc());
    }

    @Override
    public List<Product> findLowStock() {
        return catalogMapper.toProductList(productRepository.findLowStock());
    }

    @Override
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id).map(catalogMapper::toDomain);
    }

    @Override
    public Optional<Product> findByIdUpdate(Long id) {
        return productRepository.findByIdUpdate(id).map(catalogMapper::toDomain);
    }

    @Override
    public boolean existsBySku(String sku) {
        return productRepository.existsBySkuIgnoreCase(sku);
    }

    @Override
    public Product save(Product product) {
        var entity = product.getId() == null ? new ProductEntity() :
                productRepository.findById(product.getId())
                        .orElseThrow(() -> new NotFoundException("Product", product.getId()));
        catalogMapper.updateEntity(product, entity);
        return catalogMapper.toDomain(productRepository.save(entity));
    }
}
