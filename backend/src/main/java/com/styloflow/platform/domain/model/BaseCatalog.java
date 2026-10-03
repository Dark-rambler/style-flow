package com.styloflow.platform.domain.model;

import java.math.BigDecimal;
import java.util.List;

public final class BaseCatalog {

    public record ServiceSeed(
            String name,
            int durationMinutes,
            BigDecimal price
    ) {}

    public record CategorySeed(
            String name,
            List<ServiceSeed> services
    ) {}

    public static final List<CategorySeed> CATEGORIES = List.of(
            new CategorySeed("Cortes", List.of(
                    service("Corte de dama", 45, "60.00"),
                    service("Corte de caballero", 30, "35.00"),
                    service("Corte infantil", 30, "25.00"))),
            new CategorySeed("Color", List.of(
                    service("Tinte completo", 120, "180.00"),
                    service("Mechas / balayage", 180, "350.00"))),
            new CategorySeed("Tratamientos", List.of(
                    service("Hidratación profunda", 45, "90.00"),
                    service("Keratina", 150, "400.00"))),
            new CategorySeed("Peinados", List.of(
                    service("Brushing", 40, "50.00"),
                    service("Peinado de evento", 60, "120.00"))),
            new CategorySeed("Uñas", List.of(
                    service("Manicure", 40, "40.00"),
                    service("Pedicure", 50, "50.00"))));

    private BaseCatalog() {}

    private static ServiceSeed service(String name, int durationMinutes, String price) {
        return new ServiceSeed(name, durationMinutes, new BigDecimal(price));
    }
}
