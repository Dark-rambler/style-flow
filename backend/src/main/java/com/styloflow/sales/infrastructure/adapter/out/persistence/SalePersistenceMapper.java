package com.styloflow.sales.infrastructure.adapter.out.persistence;

import com.styloflow.cashregister.infrastructure.adapter.out.persistence.CashRegisterPersistenceMapper;
import com.styloflow.customers.infrastructure.adapter.out.persistence.CustomerPersistenceMapper;
import com.styloflow.sales.domain.model.ItemType;
import com.styloflow.sales.domain.model.Sale;
import com.styloflow.sales.domain.model.SaleItem;
import com.styloflow.users.infrastructure.adapter.out.persistence.UserPersistenceMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/** Associations (cash register, users, customer, service/product) are set as references by the adapter. */
@Mapper(componentModel = "spring", imports = ItemType.class,
        uses = {UserPersistenceMapper.class, CustomerPersistenceMapper.class, CashRegisterPersistenceMapper.class})
public interface SalePersistenceMapper {

    Sale toDomain(SaleEntity entity);

    @Mapping(target = "itemId", expression = """
            java(entity.getType() == ItemType.SERVICE ? entity.getService().getId() : entity.getProduct().getId())""")
    SaleItem toDomain(SaleItemEntity entity);

    @Mapping(target = "cashRegister", ignore = true)
    @Mapping(target = "cashier", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "voidedBy", ignore = true)
    @Mapping(target = "items", ignore = true)
    void updateEntity(Sale sale, @MappingTarget SaleEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sale", ignore = true)
    @Mapping(target = "service", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "stylist", ignore = true)
    SaleItemEntity toEntity(SaleItem item);
}
