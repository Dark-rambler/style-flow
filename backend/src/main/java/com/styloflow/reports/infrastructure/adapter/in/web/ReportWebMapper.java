package com.styloflow.reports.infrastructure.adapter.in.web;

import com.styloflow.reports.domain.model.DailySales;
import com.styloflow.reports.domain.model.SalesSummary;
import com.styloflow.reports.domain.model.StylistTotal;
import com.styloflow.reports.domain.model.TopItem;
import com.styloflow.reports.infrastructure.adapter.in.web.dto.DailySalesResponse;
import com.styloflow.reports.infrastructure.adapter.in.web.dto.SalesSummaryResponse;
import com.styloflow.reports.infrastructure.adapter.in.web.dto.StylistTotalResponse;
import com.styloflow.reports.infrastructure.adapter.in.web.dto.TopItemResponse;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReportWebMapper {

    SalesSummaryResponse toResponse(SalesSummary summary);

    List<DailySalesResponse> toDailySalesList(List<DailySales> days);

    List<StylistTotalResponse> toStylistList(List<StylistTotal> stylists);

    List<TopItemResponse> toTopItemList(List<TopItem> items);
}
