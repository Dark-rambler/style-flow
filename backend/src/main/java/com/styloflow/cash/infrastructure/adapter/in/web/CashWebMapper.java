package com.styloflow.cash.infrastructure.adapter.in.web;

import com.styloflow.cash.application.port.in.command.CloseCashCommand;
import com.styloflow.cash.application.port.in.command.OpenCashCommand;
import com.styloflow.cash.domain.model.CashSummary;
import com.styloflow.cash.infrastructure.adapter.in.web.dto.CashResponse;
import com.styloflow.cash.infrastructure.adapter.in.web.dto.CloseCashRequest;
import com.styloflow.cash.infrastructure.adapter.in.web.dto.OpenCashRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CashWebMapper {

    OpenCashCommand toCommand(OpenCashRequest request);

    CloseCashCommand toCommand(CloseCashRequest request);

    @Mapping(target = "id", source = "cash.id")
    @Mapping(target = "status", source = "cash.status")
    @Mapping(target = "openedBy", source = "cash.openedBy.name")
    @Mapping(target = "openedAt", source = "cash.openedAt")
    @Mapping(target = "openingAmount", source = "cash.openingAmount")
    @Mapping(target = "closedBy", source = "cash.closedBy.name")
    @Mapping(target = "closedAt", source = "cash.closedAt")
    @Mapping(target = "countedCash", source = "cash.countedCash")
    @Mapping(target = "difference", source = "cash.difference")
    @Mapping(target = "notes", source = "cash.notes")
    CashResponse toResponse(CashSummary summary);
}
