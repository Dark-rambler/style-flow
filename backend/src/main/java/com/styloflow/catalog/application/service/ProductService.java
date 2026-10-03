package com.styloflow.catalog.application.service;

import com.styloflow.catalog.application.port.in.command.ProductCommand;
import com.styloflow.catalog.application.port.in.ProductUseCase;
import com.styloflow.catalog.application.port.out.ProductRepositoryPort;
import com.styloflow.catalog.application.utils.ImageFilesUtil;
import com.styloflow.catalog.domain.model.Product;
import com.styloflow.shared.application.port.out.CurrentTenantPort;
import com.styloflow.shared.application.port.out.ImageStoragePort;
import com.styloflow.shared.domain.exception.BusinessRuleException;
import com.styloflow.shared.domain.exception.NotFoundException;
import com.styloflow.shared.domain.model.StoredImage;
import com.styloflow.shared.domain.model.TextUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Default {@link ProductUseCase} implementation. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService implements ProductUseCase {

    private final ProductRepositoryPort productRepository;
    private final ImageStoragePort imageStorage;
    private final CurrentTenantPort currentTenant;

    @Override
    public List<Product> list(boolean activeOnly) {
        return productRepository.findAllSorted(activeOnly);
    }

    @Override
    public List<Product> lowStock() {
        return productRepository.findLowStock();
    }

    @Override
    @Transactional
    public Product create(ProductCommand command) {
        String sku = TextUtils.blankToNull(command.sku());
        if (sku != null && productRepository.existsBySku(sku))
            throw new BusinessRuleException("A product with that SKU already exists");
        Product product = new Product();
        apply(product, command);
        if (command.image() == null)
            return productRepository.save(product);
        StoredImage image = imageStorage.upload(ImageFilesUtil.bytes(command.image()), "business-" + currentTenant.businessId() + "/products");
        product.setImageUrl(image.url());
        product.setImagePublicId(image.publicId());
        try {
            return productRepository.save(product);
        } catch (RuntimeException e) {
            imageStorage.delete(image.publicId());
            throw e;
        }
    }

    @Override
    @Transactional
    public Product update(Long id, ProductCommand command) {
        Product product = productRepository.findById(id).orElseThrow(() -> new NotFoundException("Product", id));
        apply(product, command);
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public Product adjustStock(Long id, int quantity) {
        Product product = productRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new NotFoundException("Product", id));
        product.adjustStock(quantity);
        return productRepository.save(product);
    }

    private static void apply(Product product, ProductCommand command) {
        String sku = TextUtils.blankToNull(command.sku());
        product.setName(command.name().trim());
        product.setSku(sku != null ? sku.toUpperCase() : null);
        product.setPrice(command.price());
        product.setStock(command.stock());
        product.setMinStock(command.minStock() != null ? command.minStock() : 0);
        if (command.active() != null)
            product.setActive(command.active());
    }
}
