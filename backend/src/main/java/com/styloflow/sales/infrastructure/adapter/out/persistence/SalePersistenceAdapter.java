package com.styloflow.sales.infrastructure.adapter.out.persistence;

import com.styloflow.cashregister.application.port.out.SalesTotalsPort;
import com.styloflow.cashregister.domain.model.PaymentTotal;
import com.styloflow.cashregister.infrastructure.adapter.out.persistence.CashRegisterEntity;
import com.styloflow.catalog.infrastructure.adapter.out.persistence.ProductEntity;
import com.styloflow.catalog.infrastructure.adapter.out.persistence.SalonServiceEntity;
import com.styloflow.customers.infrastructure.adapter.out.persistence.CustomerEntity;
import com.styloflow.sales.application.port.out.SaleRepositoryPort;
import com.styloflow.sales.domain.model.ItemType;
import com.styloflow.sales.domain.model.Sale;
import com.styloflow.sales.domain.model.SaleItem;
import com.styloflow.sales.domain.model.SaleStatus;
import com.styloflow.shared.domain.exception.NotFoundException;
import com.styloflow.shared.domain.model.PageResult;
import com.styloflow.shared.infrastructure.persistence.PageResults;
import com.styloflow.users.domain.model.User;
import com.styloflow.users.infrastructure.adapter.out.persistence.UserEntity;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SalePersistenceAdapter implements SaleRepositoryPort, SalesTotalsPort {

    private final SaleJpaRepository saleRepository;
    private final SalePersistenceMapper saleMapper;
    private final EntityManager entityManager;

    @Override
    public Optional<Sale> findDetail(Long id) {
        return saleRepository.findDetail(id).map(saleMapper::toDomain);
    }

    @Override
    public PageResult<Sale> search(Instant from, Instant to, Long customerId, int page, int size) {
        return PageResults.of(saleRepository.search(from, to, customerId, PageRequest.of(page, size)),
                saleMapper::toDomain);
    }

    @Override
    public List<PaymentTotal> totalsByPaymentMethod(Long cashRegisterId) {
        return saleRepository.totalsByPaymentMethod(cashRegisterId, SaleStatus.COMPLETED).stream()
                .map(t -> new PaymentTotal(t.getPaymentMethod(), t.getCount(), t.getTotal()))
                .toList();
    }

    /** A sale is created with its items; afterwards only its status changes (void). */
    @Override
    public Sale save(Sale sale) {
        SaleEntity entity = sale.getId() == null ? new SaleEntity()
                : saleRepository.findById(sale.getId()).orElseThrow(() -> new NotFoundException("Sale", sale.getId()));
        saleMapper.updateEntity(sale, entity);
        entity.setCashRegister(entityManager.getReference(CashRegisterEntity.class, sale.getCashRegister().getId()));
        entity.setCashier(user(sale.getCashier()));
        entity.setCustomer(sale.getCustomer() != null
                ? entityManager.getReference(CustomerEntity.class, sale.getCustomer().getId()) : null);
        entity.setVoidedBy(user(sale.getVoidedBy()));
        if (entity.getId() == null) {
            sale.getItems().forEach(item -> entity.addItem(toEntity(item)));
        }
        return saleMapper.toDomain(saleRepository.save(entity));
    }

    private SaleItemEntity toEntity(SaleItem item) {
        SaleItemEntity entity = saleMapper.toEntity(item);
        if (item.getType() == ItemType.SERVICE) {
            entity.setService(entityManager.getReference(SalonServiceEntity.class, item.getItemId()));
        } else {
            entity.setProduct(entityManager.getReference(ProductEntity.class, item.getItemId()));
        }
        entity.setStylist(user(item.getStylist()));
        return entity;
    }

    private UserEntity user(User user) {
        return user != null ? entityManager.getReference(UserEntity.class, user.getId()) : null;
    }
}
