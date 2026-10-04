package com.styloflow.sales.infrastructure.adapter.in.web;

import com.styloflow.sales.application.port.in.command.RegisterSaleCommand;
import com.styloflow.sales.domain.model.SaleItemModel;
import com.styloflow.sales.domain.model.SaleModel;
import com.styloflow.sales.infrastructure.adapter.in.web.dto.SaleItemResponse;
import com.styloflow.sales.infrastructure.adapter.in.web.dto.SaleRequest;
import com.styloflow.sales.infrastructure.adapter.in.web.dto.SaleResponse;
import com.styloflow.sales.infrastructure.adapter.in.web.dto.SaleSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SaleWebMapper {

    RegisterSaleCommand toCommand(SaleRequest request);

    @Mapping(target = "cashId", source = "cash.id")
    @Mapping(target = "cashier", source = "cashier.name")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customer", source = "customer.name")
    @Mapping(target = "customerTaxId", source = "customer.taxId")
    @Mapping(target = "voidedBy", source = "voidedBy.name")
    SaleResponse toResponse(SaleModel sale);

    @Mapping(target = "stylistId", source = "stylist.id")
    @Mapping(target = "stylist", source = "stylist.name")
    SaleItemResponse toResponse(SaleItemModel item);

    @Mapping(target = "cashier", source = "cashier.name")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customer", source = "customer.name")
    @Mapping(target = "cashOpen", source = "cash.open")
    SaleSummaryResponse toSummary(SaleModel sale);
}
