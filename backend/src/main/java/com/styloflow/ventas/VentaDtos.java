package com.styloflow.ventas;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class VentaDtos {

    private VentaDtos() {}

    public record ItemRequest(
            @NotNull VentaItem.Tipo tipo,
            /** id del servicio o producto según {@code tipo}. */
            @NotNull Long itemId,
            @NotNull @Min(1) @Max(999) Integer cantidad,
            /** Opcional: precio distinto al de catálogo (p. ej. cabello largo). */
            @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal precioUnitario,
            Long estilistaId) {}

    public record VentaRequest(
            Long clienteId,
            @NotEmpty @Size(max = 50) List<@Valid ItemRequest> items,
            @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal descuento,
            @NotNull MetodoPago metodoPago,
            @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal montoRecibido,
            @Size(max = 500) String observaciones) {}

    public record AnularRequest(@NotBlank @Size(max = 250) String motivo) {}

    public record ItemResponse(Long id, VentaItem.Tipo tipo, Long itemId, String descripcion, int cantidad,
            BigDecimal precioUnitario, BigDecimal subtotal, Long estilistaId, String estilista) {

        static ItemResponse from(VentaItem i) {
            Long itemId = i.getTipo() == VentaItem.Tipo.SERVICIO ? i.getServicio().getId() : i.getProducto().getId();
            return new ItemResponse(i.getId(), i.getTipo(), itemId, i.getDescripcion(), i.getCantidad(),
                    i.getPrecioUnitario(), i.getSubtotal(),
                    i.getEstilista() != null ? i.getEstilista().getId() : null,
                    i.getEstilista() != null ? i.getEstilista().getNombre() : null);
        }
    }

    public record VentaResumen(Long id, Instant fecha, String cajero, Long clienteId, String cliente,
            BigDecimal total, MetodoPago metodoPago, Venta.Estado estado) {

        static VentaResumen from(Venta v) {
            return new VentaResumen(v.getId(), v.getFecha(), v.getCajero().getNombre(),
                    v.getCliente() != null ? v.getCliente().getId() : null,
                    v.getCliente() != null ? v.getCliente().getNombre() : null,
                    v.getTotal(), v.getMetodoPago(), v.getEstado());
        }
    }

    public record VentaResponse(Long id, Instant fecha, Long cajaId, String cajero, Long clienteId, String cliente,
            String clienteCiNit, BigDecimal subtotal, BigDecimal descuento, BigDecimal total, BigDecimal iva,
            MetodoPago metodoPago, BigDecimal montoRecibido, BigDecimal cambio, Venta.Estado estado,
            String anuladaPor, Instant anuladaEn, String motivoAnulacion, String observaciones,
            List<ItemResponse> items) {

        static VentaResponse from(Venta v) {
            return new VentaResponse(v.getId(), v.getFecha(), v.getCaja().getId(), v.getCajero().getNombre(),
                    v.getCliente() != null ? v.getCliente().getId() : null,
                    v.getCliente() != null ? v.getCliente().getNombre() : null,
                    v.getCliente() != null ? v.getCliente().getCiNit() : null,
                    v.getSubtotal(), v.getDescuento(), v.getTotal(), v.getIva(), v.getMetodoPago(),
                    v.getMontoRecibido(), v.getCambio(), v.getEstado(),
                    v.getAnuladaPor() != null ? v.getAnuladaPor().getNombre() : null,
                    v.getAnuladaEn(), v.getMotivoAnulacion(), v.getObservaciones(),
                    v.getItems().stream().map(ItemResponse::from).toList());
        }
    }
}
