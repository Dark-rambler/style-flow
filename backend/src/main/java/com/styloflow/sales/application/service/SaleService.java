package com.styloflow.sales.application.service;

import com.styloflow.business.application.port.in.BusinessUseCase;
import com.styloflow.cashregister.application.port.out.CashRegisterRepositoryPort;
import com.styloflow.cashregister.domain.exception.NoOpenCashRegisterException;
import com.styloflow.cashregister.domain.model.CashRegister;
import com.styloflow.catalog.application.port.out.ProductRepositoryPort;
import com.styloflow.catalog.application.port.out.SalonServiceRepositoryPort;
import com.styloflow.catalog.domain.model.Product;
import com.styloflow.catalog.domain.model.SalonService;
import com.styloflow.customers.application.port.out.CustomerRepositoryPort;
import com.styloflow.customers.domain.model.Customer;
import com.styloflow.sales.application.port.in.RegisterSaleCommand;
import com.styloflow.sales.application.port.in.SaleUseCase;
import com.styloflow.sales.application.port.out.SaleRepositoryPort;
import com.styloflow.sales.domain.model.ItemType;
import com.styloflow.sales.domain.model.Sale;
import com.styloflow.sales.domain.model.SaleItem;
import com.styloflow.shared.domain.exception.BusinessRuleException;
import com.styloflow.shared.domain.exception.NotFoundException;
import com.styloflow.shared.domain.model.DateRange;
import com.styloflow.shared.domain.model.PageResult;
import com.styloflow.users.application.port.out.UserRepositoryPort;
import com.styloflow.users.domain.model.User;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Registers and voids sales, keeping product stock in sync. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SaleService implements SaleUseCase {

    private final SaleRepositoryPort saleRepository;
    private final CashRegisterRepositoryPort cashRegisterRepository;
    private final SalonServiceRepositoryPort serviceRepository;
    private final ProductRepositoryPort productRepository;
    private final UserRepositoryPort userRepository;
    private final CustomerRepositoryPort customerRepository;
    private final BusinessUseCase businessUseCase;
    private final Clock clock;
    private final ZoneId zone;

    @Override
    @Transactional
    public Sale register(RegisterSaleCommand command, Long cashierId) {
        CashRegister cashRegister = cashRegisterRepository.findOpen().orElseThrow(NoOpenCashRegisterException::new);
        User cashier = getUserOrThrow(cashierId);
        Customer customer = command.customerId() == null ? null
                : customerRepository.findById(command.customerId())
                        .orElseThrow(() -> new NotFoundException("Customer", command.customerId()));
        List<SaleItem> items = command.items().stream().map(this::createItem).toList();
        Sale sale = Sale.register(clock.instant(), cashRegister, cashier, customer, items, command.discount(),
                command.paymentMethod(), command.amountReceived(), businessUseCase.getCurrent().getTaxRate(),
                command.notes());
        return saleRepository.save(sale);
    }

    private SaleItem createItem(RegisterSaleCommand.Item item) {
        if (item.type() == ItemType.SERVICE) {
            SalonService service = serviceRepository.findById(item.itemId())
                    .orElseThrow(() -> new NotFoundException("Service", item.itemId()));
            if (!service.isActive()) {
                throw new BusinessRuleException("The service '" + service.getName() + "' is not active");
            }
            return SaleItem.create(ItemType.SERVICE, service.getId(), service.getName(), item.quantity(),
                    item.unitPrice() != null ? item.unitPrice() : service.getPrice(), item.discount(),
                    stylist(item.stylistId()));
        }
        Product product = productRepository.findByIdForUpdate(item.itemId())
                .orElseThrow(() -> new NotFoundException("Product", item.itemId()));
        if (!product.isActive()) {
            throw new BusinessRuleException("The product '" + product.getName() + "' is not active");
        }
        product.decreaseStock(item.quantity());
        productRepository.save(product);
        return SaleItem.create(ItemType.PRODUCT, product.getId(), product.getName(), item.quantity(),
                item.unitPrice() != null ? item.unitPrice() : product.getPrice(), item.discount(),
                stylist(item.stylistId()));
    }

    private User stylist(Long stylistId) {
        if (stylistId == null) {
            return null;
        }
        return userRepository.findById(stylistId)
                .filter(User::isActive)
                .orElseThrow(() -> new BusinessRuleException("Invalid or inactive stylist: " + stylistId));
    }

    @Override
    @Transactional
    public Sale voidSale(Long id, String reason, Long userId) {
        Sale sale = getSaleOrThrow(id);
        sale.voidSale(getUserOrThrow(userId), reason, clock.instant());
        sale.getItems().stream()
                .filter(item -> item.getType() == ItemType.PRODUCT)
                .forEach(item -> productRepository.findByIdForUpdate(item.getItemId()).ifPresent(product -> {
                    product.increaseStock(item.getQuantity());
                    productRepository.save(product);
                }));
        return saleRepository.save(sale);
    }

    @Override
    public Sale get(Long id) {
        return getSaleOrThrow(id);
    }

    @Override
    public PageResult<Sale> search(LocalDate from, LocalDate to, Long customerId, int page, int size) {
        DateRange range = DateRange.of(from, to, LocalDate.now(clock.withZone(zone)));
        return saleRepository.search(range.start(zone), range.end(zone), customerId, page, Math.min(size, 100));
    }

    private Sale getSaleOrThrow(Long id) {
        return saleRepository.findDetail(id).orElseThrow(() -> new NotFoundException("Sale", id));
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User", id));
    }
}
