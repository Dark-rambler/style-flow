package com.styloflow.platform.domain.model;

import java.util.List;

/** Already normalized data to register a business with its administrator and starter catalog. */
public record NewBusiness(String code, String name, String taxId, String phone, String adminName,
        String adminUsername, String adminPasswordHash, List<BaseCatalog.CategorySeed> catalog) {}
