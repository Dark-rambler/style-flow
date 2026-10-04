package com.styloflow.sales.application.service;

import com.styloflow.business.application.port.in.BusinessUseCase;
import com.styloflow.cash.application.port.out.CashRepositoryPort;
import com.styloflow.cash.domain.exception.NoOpenCashException;
import com.styloflow.catalog.application.port.out.ProductRepositoryPort;
import com.styloflow.catalog.application.port.out.ServiceRepositoryPort;
import com.styloflow.customers.application.port.out.CustomerRepositoryPort;
import com.styloflow.sales.application.port.in.command.RegisterSaleCommand;
import com.styloflow.sales.application.port.in.SaleUseCase;
import com.styloflow.sales.application.port.out.SaleRepositoryPort;
import com.styloflow.sales.domain.enums.ItemType;
import com.styloflow.sales.domain.model.SaleModel;
import com.styloflow.sales.domain.model.SaleItemModel;
import com.styloflow.shared.domain.exception.BusinessRuleException;
import com.styloflow.shared.domain.exception.NotFoundException;
import com.styloflow.shared.domain.model.DateRange;
import com.styloflow.shared.domain.model.PageResult;
import com.styloflow.users.application.port.out.UserRepositoryPort;
import com.styloflow.users.domain.model.UserModel;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SaleService implements SaleUseCase {

    private final SaleRepositoryPort saleRepository;
    private final CashRepositoryPort cashRepository;
    private final ServiceRepositoryPort serviceRepository;
    private final ProductRepositoryPort productRepository;
    private final UserRepositoryPort userRepository;
    private final CustomerRepositoryPort customerRepository;
    private final BusinessUseCase businessUseCase;
    private final Clock clock;
    private final ZoneId zone;

    @Override
    @Transactional
    public SaleModel register(RegisterSaleCommand command, Long cashierId) {
        var cash = cashRepository.findOpen().orElseThrow(NoOpenCashException::new);
        var cashier = getUserOrThrow(cashierId);
        var customer = command.customerId() == null ? null :
                customerRepository.findById(command.customerId())
                        .orElseThrow(() -> new NotFoundException("Customer", command.customerId()));
        var items = command.items().stream().map(this::createItem).toList();
        var sale = SaleModel.register(
                clock.instant(),
                cash,
                cashier,
                customer,
                items,
                command.discount(),
                command.paymentMethod(),
                command.amountReceived(),
                businessUseCase.getCurrent().getTaxRate(),
                command.notes());
        return saleRepository.save(sale);
    }

    private SaleItemModel createItem(RegisterSaleCommand.Item item) {
        if (item.type() == ItemType.SERVICE) {
            var service = serviceRepository.findById(item.itemId())
                    .orElseThrow(() -> new NotFoundException("Service", item.itemId()));
            if (!service.isActive())
                throw new BusinessRuleException("The service '" + service.getName() + "' is not active");
            return SaleItemModel.create(
                    ItemType.SERVICE,
                    service.getId(),
                    service.getName(),
                    item.quantity(),
                    item.unitPrice() != null ? item.unitPrice() : service.getPrice(),
                    item.discount(),
                    stylist(item.stylistId())
            );
        }
        var product = productRepository.findByIdUpdate(item.itemId())
                .orElseThrow(() -> new NotFoundException("Product", item.itemId()));
        if (!product.isActive())
            throw new BusinessRuleException("The product '" + product.getName() + "' is not active");
        product.decreaseStock(item.quantity());
        productRepository.save(product);
        return SaleItemModel.create(
                ItemType.PRODUCT,
                product.getId(),
                product.getName(),
                item.quantity(),
                item.unitPrice() != null ? item.unitPrice() : product.getPrice(),
                item.discount(),
                stylist(item.stylistId())
        );
    }

    private UserModel stylist(Long stylistId) {
        if (stylistId == null)
            return null;
        return userRepository.findById(stylistId)
                .filter(UserModel::isActive)
                .orElseThrow(() -> new BusinessRuleException("Invalid or inactive stylist: " + stylistId));
    }

    @Override
    @Transactional
    public SaleModel voidSale(Long id, String reason, Long userId) {
        var sale = getSaleOrThrow(id);
        sale.voidSale(getUserOrThrow(userId), reason, clock.instant());
        sale.getItems().stream()
                .filter(item -> item.getType() == ItemType.PRODUCT)
                .forEach(item -> productRepository.findByIdUpdate(item.getItemId())
                        .ifPresent(product -> {
                            product.increaseStock(item.getQuantity());
                            productRepository.save(product);
                }));
        return saleRepository.save(sale);
    }

    @Override
    public SaleModel get(Long id) {
        return getSaleOrThrow(id);
    }

    @Override
    public PageResult<SaleModel> search(LocalDate from, LocalDate to, Long customerId, int page, int size) {
        var range = DateRange.of(from, to, LocalDate.now(clock.withZone(zone)));
        return saleRepository.search(range.start(zone), range.end(zone), customerId, page, Math.min(size, 100));
    }

    private SaleModel getSaleOrThrow(Long id) {
        return saleRepository.findDetail(id).orElseThrow(() -> new NotFoundException("Sale", id));
    }

    private UserModel getUserOrThrow(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User", id));
    }
}
