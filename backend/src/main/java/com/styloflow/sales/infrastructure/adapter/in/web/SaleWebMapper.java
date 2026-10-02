package com.styloflow.sales.infrastructure.adapter.in.web;

import com.styloflow.sales.application.port.in.RegisterSaleCommand;
import com.styloflow.sales.domain.model.Sale;
import com.styloflow.sales.domain.model.SaleItem;
import com.styloflow.sales.infrastructure.adapter.in.web.dto.SaleItemResponse;
import com.styloflow.sales.infrastructure.adapter.in.web.dto.SaleRequest;
import com.styloflow.sales.infrastructure.adapter.in.web.dto.SaleResponse;
import com.styloflow.sales.infrastructure.adapter.in.web.dto.SaleSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SaleWebMapper {

    RegisterSaleCommand toCommand(SaleRequest request);

    @Mapping(target = "cashRegisterId", source = "cashRegister.id")
    @Mapping(target = "cashier", source = "cashier.name")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customer", source = "customer.name")
    @Mapping(target = "customerTaxId", source = "customer.taxId")
    @Mapping(target = "voidedBy", source = "voidedBy.name")
    SaleResponse toResponse(Sale sale);

    @Mapping(target = "stylistId", source = "stylist.id")
    @Mapping(target = "stylist", source = "stylist.name")
    SaleItemResponse toResponse(SaleItem item);

    @Mapping(target = "cashier", source = "cashier.name")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customer", source = "customer.name")
    @Mapping(target = "cashRegisterOpen", source = "cashRegister.open")
    SaleSummaryResponse toSummary(Sale sale);
}
