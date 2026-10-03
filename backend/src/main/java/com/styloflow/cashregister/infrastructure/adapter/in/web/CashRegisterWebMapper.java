package com.styloflow.cashregister.infrastructure.adapter.in.web;

import com.styloflow.cashregister.application.port.in.command.CloseCashRegisterCommand;
import com.styloflow.cashregister.application.port.in.command.OpenCashRegisterCommand;
import com.styloflow.cashregister.domain.model.CashRegisterSummary;
import com.styloflow.cashregister.infrastructure.adapter.in.web.dto.CashRegisterResponse;
import com.styloflow.cashregister.infrastructure.adapter.in.web.dto.CloseCashRegisterRequest;
import com.styloflow.cashregister.infrastructure.adapter.in.web.dto.OpenCashRegisterRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CashRegisterWebMapper {

    OpenCashRegisterCommand toCommand(OpenCashRegisterRequest request);

    CloseCashRegisterCommand toCommand(CloseCashRegisterRequest request);

    @Mapping(target = "id", source = "cashRegister.id")
    @Mapping(target = "status", source = "cashRegister.status")
    @Mapping(target = "openedBy", source = "cashRegister.openedBy.name")
    @Mapping(target = "openedAt", source = "cashRegister.openedAt")
    @Mapping(target = "openingAmount", source = "cashRegister.openingAmount")
    @Mapping(target = "closedBy", source = "cashRegister.closedBy.name")
    @Mapping(target = "closedAt", source = "cashRegister.closedAt")
    @Mapping(target = "countedCash", source = "cashRegister.countedCash")
    @Mapping(target = "difference", source = "cashRegister.difference")
    @Mapping(target = "notes", source = "cashRegister.notes")
    CashRegisterResponse toResponse(CashRegisterSummary summary);
}
