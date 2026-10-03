package com.styloflow.reports.infrastructure.adapter.out.persistence;

import com.styloflow.reports.application.port.out.ReportQueryPort;
import com.styloflow.reports.domain.model.DailySales;
import com.styloflow.reports.domain.model.ExportedSale;
import com.styloflow.reports.domain.model.PaymentMethodTotal;
import com.styloflow.reports.domain.model.SalesTotals;
import com.styloflow.reports.domain.model.StylistTotal;
import com.styloflow.reports.domain.model.TopItem;
import com.styloflow.sales.domain.enums.ItemType;
import com.styloflow.sales.infrastructure.adapter.out.persistence.ItemTypeConverter;
import com.styloflow.sales.infrastructure.adapter.out.persistence.PaymentMethodConverter;
import com.styloflow.sales.infrastructure.adapter.out.persistence.SaleStatusConverter;
import com.styloflow.shared.application.port.out.CurrentTenantPort;
import com.styloflow.shared.domain.model.DateRange;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReportJdbcAdapter implements ReportQueryPort {

    private static final String ITEM_AMOUNT = "COALESCE(i.subtotal * v.total / NULLIF(v.subtotal, 0), 0)";

    private static final PaymentMethodConverter PAYMENT_METHODS = new PaymentMethodConverter();
    private static final SaleStatusConverter SALE_STATUSES = new SaleStatusConverter();
    private static final ItemTypeConverter ITEM_TYPES = new ItemTypeConverter();

    private final JdbcClient jdbc;
    private final ZoneId zone;
    private final CurrentTenantPort currentTenant;

    @Override
    public SalesTotals totals(DateRange range) {
        return jdbc.sql("""
                        SELECT COUNT(*) FILTER (WHERE estado = 'COMPLETADA')                    AS count,
                               COALESCE(SUM(total) FILTER (WHERE estado = 'COMPLETADA'), 0)     AS total,
                               COALESCE(SUM(descuento) FILTER (WHERE estado = 'COMPLETADA'), 0) AS discounts,
                               COALESCE(SUM(iva) FILTER (WHERE estado = 'COMPLETADA'), 0)       AS tax,
                               COUNT(*) FILTER (WHERE estado = 'ANULADA')                       AS voided
                        FROM ventas
                        WHERE negocio_id = :businessId AND fecha >= :from AND fecha < :to
                        """)
                .params(params(range))
                .query((rs, n) -> new SalesTotals(
                        rs.getLong("count"),
                        rs.getBigDecimal("total"),
                        rs.getBigDecimal("discounts"),
                        rs.getBigDecimal("tax"),
                        rs.getLong("voided"))
                )
                .single();
    }

    @Override
    public List<PaymentMethodTotal> totalsByPaymentMethod(DateRange range) {
        return jdbc.sql("""
                        SELECT metodo_pago, COUNT(*) AS count, SUM(total) AS total
                        FROM ventas
                        WHERE negocio_id = :businessId AND estado = 'COMPLETADA' AND fecha >= :from AND fecha < :to
                        GROUP BY metodo_pago
                        ORDER BY total DESC
                        """)
                .params(params(range))
                .query((rs, n) -> new PaymentMethodTotal(PAYMENT_METHODS.convertToEntityAttribute(
                        rs.getString(1)),
                        rs.getLong(2),
                        rs.getBigDecimal(3))
                )
                .list();
    }

    @Override
    public List<DailySales> salesByDay(DateRange range) {
        return jdbc.sql("""
                        SELECT CAST(fecha AT TIME ZONE :tz AS date) AS day, COUNT(*) AS count, SUM(total) AS total
                        FROM ventas
                        WHERE negocio_id = :businessId AND estado = 'COMPLETADA' AND fecha >= :from AND fecha < :to
                        GROUP BY day
                        ORDER BY day
                        """)
                .params(params(range))
                .param("tz", zone.getId())
                .query((rs, n) -> new DailySales(
                        rs.getDate(1).toLocalDate(),
                        rs.getLong(2),
                        rs.getBigDecimal(3))
                )
                .list();
    }

    @Override
    public List<StylistTotal> byStylist(DateRange range, Long stylistId) {
        return jdbc.sql("""
                        SELECT u.id,
                               u.nombre,
                               SUM(i.cantidad) AS services,
                               ROUND(SUM(%s), 2) AS total,
                               u.comision_porcentaje
                        FROM venta_items i
                        JOIN ventas   v ON v.id = i.venta_id
                        JOIN usuarios u ON u.id = i.estilista_id
                        WHERE v.negocio_id = :businessId
                          AND v.estado = 'COMPLETADA'
                          AND v.fecha >= :from
                          AND v.fecha < :to
                          AND i.tipo = 'SERVICIO'
                          AND (CAST(:stylistId AS BIGINT) IS NULL OR u.id = CAST(:stylistId AS BIGINT))
                        GROUP BY u.id, u.nombre, u.comision_porcentaje
                        ORDER BY total DESC
                        """.formatted(ITEM_AMOUNT))
                .params(params(range))
                .param("stylistId", stylistId, Types.BIGINT)
                .query((rs, n) -> StylistTotal.of(
                        rs.getLong("id"),
                        rs.getString("nombre"),
                        rs.getLong("services"),
                        rs.getBigDecimal("total"),
                        rs.getBigDecimal("comision_porcentaje"))
                )
                .list();
    }

    @Override
    public List<TopItem> topItems(DateRange range, ItemType type, int limit) {
        String column = type == ItemType.SERVICE ? "servicio_id" : "producto_id";
        return jdbc.sql("""
                        SELECT i.%1$s AS id,
                               MAX(i.descripcion) AS name,
                               SUM(i.cantidad) AS quantity,
                               ROUND(SUM(%2$s), 2) AS total
                        FROM venta_items i
                        JOIN ventas v ON v.id = i.venta_id
                        WHERE v.negocio_id = :businessId
                          AND v.estado = 'COMPLETADA'
                          AND v.fecha >= :from
                          AND v.fecha < :to
                          AND i.tipo = :type
                        GROUP BY i.%1$s
                        ORDER BY quantity DESC, total DESC
                        LIMIT :limit
                        """.formatted(column, ITEM_AMOUNT))
                .params(params(range))
                .param("type", ITEM_TYPES.convertToDatabaseColumn(type))
                .param("limit", limit)
                .query((rs, n) -> new TopItem(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getLong("quantity"),
                        rs.getBigDecimal("total"))
                )
                .list();
    }

    @Override
    public void sales(DateRange range, Consumer<ExportedSale> consumer) {
        jdbc.sql("""
                        SELECT v.id,
                               v.fecha,
                               u.nombre AS cashier,
                               c.nombre AS customer,
                               v.subtotal,
                               v.descuento,
                               v.total,
                               v.iva,
                               v.metodo_pago,
                               v.estado
                        FROM ventas v
                        JOIN usuarios u ON u.id = v.cajero_id
                        LEFT JOIN clientes c ON c.id = v.cliente_id
                        WHERE v.negocio_id = :businessId AND v.fecha >= :from AND v.fecha < :to
                        ORDER BY v.fecha
                        """)
                .params(params(range))
                .query(rs -> {consumer.accept(new ExportedSale(
                        rs.getLong("id"),
                        rs.getTimestamp("fecha").toInstant(),
                        rs.getString("cashier"),
                        rs.getString("customer"),
                        rs.getBigDecimal("subtotal"),
                        rs.getBigDecimal("descuento"),
                        rs.getBigDecimal("total"),
                        rs.getBigDecimal("iva"),
                        PAYMENT_METHODS.convertToEntityAttribute(rs.getString("metodo_pago")),
                        SALE_STATUSES.convertToEntityAttribute(rs.getString("estado"))));
                });
    }

    private Map<String, Object> params(DateRange range) {
        return Map.of("businessId", currentTenant.businessId(),
                "from", Timestamp.from(range.start(zone)),
                "to", Timestamp.from(range.end(zone)));
    }
}
