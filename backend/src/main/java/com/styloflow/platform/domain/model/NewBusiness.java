package com.styloflow.platform.domain.model;

import java.util.List;

public record NewBusiness(
        String code,
        String name,
        String taxId,
        String phone,
        String adminName,
        String adminUsername,
        String adminPasswordHash,
        List<BaseCatalog.CategorySeed> catalog
) {}
