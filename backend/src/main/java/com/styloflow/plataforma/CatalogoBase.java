package com.styloflow.plataforma;

import java.math.BigDecimal;
import java.util.List;

/** Catálogo inicial opcional para un negocio nuevo (el mismo del negocio demo, ver V2__seed.sql). */
final class CatalogoBase {

    record ServicioBase(String nombre, int duracionMin, BigDecimal precio) {}

    record CategoriaBase(String nombre, List<ServicioBase> servicios) {}

    static final List<CategoriaBase> CATEGORIAS = List.of(
            new CategoriaBase("Cortes", List.of(
                    s("Corte de dama", 45, "60.00"),
                    s("Corte de caballero", 30, "35.00"),
                    s("Corte infantil", 30, "25.00"))),
            new CategoriaBase("Color", List.of(
                    s("Tinte completo", 120, "180.00"),
                    s("Mechas / balayage", 180, "350.00"))),
            new CategoriaBase("Tratamientos", List.of(
                    s("Hidratación profunda", 45, "90.00"),
                    s("Keratina", 150, "400.00"))),
            new CategoriaBase("Peinados", List.of(
                    s("Brushing", 40, "50.00"),
                    s("Peinado de evento", 60, "120.00"))),
            new CategoriaBase("Uñas", List.of(
                    s("Manicure", 40, "40.00"),
                    s("Pedicure", 50, "50.00"))));

    private CatalogoBase() {}

    private static ServicioBase s(String nombre, int duracion, String precio) {
        return new ServicioBase(nombre, duracion, new BigDecimal(precio));
    }
}
