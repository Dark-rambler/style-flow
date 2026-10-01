package com.styloflow.catalogo;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public final class CatalogoDtos {

    private CatalogoDtos() {}

    public record CategoriaRequest(@NotBlank @Size(max = 80) String nombre, Boolean activo) {}

    public record CategoriaResponse(Long id, String nombre, boolean activo) {

        static CategoriaResponse from(Categoria c) {
            return new CategoriaResponse(c.getId(), c.getNombre(), c.isActivo());
        }
    }

    public record ServicioRequest(
            @NotNull Long categoriaId,
            @NotBlank @Size(max = 120) String nombre,
            @Size(max = 500) String descripcion,
            @NotNull @Min(1) Integer duracionMin,
            @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal precio,
            Boolean activo) {}

    public record ServicioResponse(Long id, Long categoriaId, String categoria, String nombre, String descripcion,
            int duracionMin, BigDecimal precio, boolean activo) {

        static ServicioResponse from(Servicio s) {
            return new ServicioResponse(s.getId(), s.getCategoria().getId(), s.getCategoria().getNombre(),
                    s.getNombre(), s.getDescripcion(), s.getDuracionMin(), s.getPrecio(), s.isActivo());
        }
    }

    public record ProductoRequest(
            @NotBlank @Size(max = 120) String nombre,
            @Size(max = 50) String sku,
            @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal precio,
            @NotNull @Min(0) Integer stock,
            @Min(0) Integer stockMinimo,
            Boolean activo) {}

    public record AjusteStockRequest(@NotNull Integer cantidad) {}

    public record ProductoResponse(Long id, String nombre, String sku, BigDecimal precio, int stock, int stockMinimo,
            boolean activo, boolean stockBajo) {

        static ProductoResponse from(Producto p) {
            return new ProductoResponse(p.getId(), p.getNombre(), p.getSku(), p.getPrecio(), p.getStock(),
                    p.getStockMinimo(), p.isActivo(), p.getStock() <= p.getStockMinimo());
        }
    }
}
