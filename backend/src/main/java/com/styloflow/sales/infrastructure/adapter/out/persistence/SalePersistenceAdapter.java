package com.styloflow.sales.infrastructure.adapter.out.persistence;

import com.styloflow.cash.application.port.out.SalesTotalsPort;
import com.styloflow.cash.domain.model.PaymentTotal;
import com.styloflow.cash.infrastructure.adapter.out.persistence.CashEntity;
import com.styloflow.cash.infrastructure.adapter.out.persistence.CashJpaRepository;
import com.styloflow.catalog.infrastructure.adapter.out.persistence.ProductEntity;
import com.styloflow.catalog.infrastructure.adapter.out.persistence.ProductJpaRepository;
import com.styloflow.catalog.infrastructure.adapter.out.persistence.ServiceEntity;
import com.styloflow.catalog.infrastructure.adapter.out.persistence.ServiceJpaRepository;
import com.styloflow.customers.infrastructure.adapter.out.persistence.CustomerEntity;
import com.styloflow.customers.infrastructure.adapter.out.persistence.CustomerJpaRepository;
import com.styloflow.sales.application.port.out.SaleRepositoryPort;
import com.styloflow.sales.domain.enums.ItemType;
import com.styloflow.sales.domain.model.Sale;
import com.styloflow.sales.domain.model.SaleItem;
import com.styloflow.sales.domain.enums.SaleStatus;
import com.styloflow.shared.domain.exception.NotFoundException;
import com.styloflow.shared.domain.model.PageResult;
import com.styloflow.shared.infrastructure.persistence.PageResults;
import com.styloflow.users.domain.model.User;
import com.styloflow.users.infrastructure.adapter.out.persistence.UserEntity;
import com.styloflow.users.infrastructure.adapter.out.persistence.UserJpaRepository;
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
    private final CashJpaRepository cashRepository;
    private final CustomerJpaRepository  customerRepository;
    private final ProductJpaRepository productRepository;
    private final ServiceJpaRepository serviceRepository;
    private final UserJpaRepository userRepository;

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
    public List<PaymentTotal> totalsByPaymentMethod(Long cashId) {
        return saleRepository.totalsByPaymentMethod(cashId, SaleStatus.COMPLETED).stream()
                .map(t -> new PaymentTotal(t.getPaymentMethod(), t.getCount(), t.getTotal()))
                .toList();
    }

    @Override
    public Sale save(Sale sale) {
        var entity = sale.getId() == null ? new SaleEntity()
                : saleRepository.findById(sale.getId()).orElseThrow(() -> new NotFoundException("Sale", sale.getId()));
        saleMapper.updateEntity(sale, entity);
        entity.setCash(cashRepository.getReferenceById(sale.getCash().getId()));
        entity.setCashier(user(sale.getCashier()));
        entity.setCustomer(sale.getCustomer() != null ? customerRepository.getReferenceById(sale.getCustomer().getId()) : null);
        entity.setVoidedBy(user(sale.getVoidedBy()));
        if (entity.getId() == null)
            sale.getItems().forEach(item -> entity.addItem(toEntity(item)));
        return saleMapper.toDomain(saleRepository.save(entity));
    }

    private SaleItemEntity toEntity(SaleItem item) {
        var entity = saleMapper.toEntity(item);
        if (item.getType() == ItemType.SERVICE)
            entity.setService(serviceRepository.getReferenceById(item.getItemId()));
        else
            entity.setProduct(productRepository.getReferenceById(item.getItemId()));
        entity.setStylist(user(item.getStylist()));
        return entity;
    }

    private UserEntity user(User user) {
        return user != null ? userRepository.getReferenceById(user.getId()) : null;
    }
}
