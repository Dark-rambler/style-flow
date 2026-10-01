package com.styloflow.reportes;

import com.styloflow.common.BusinessException;
import com.styloflow.tenant.TenantContext;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Reportes agregados sobre ventas COMPLETADAS. Rango de fechas inclusivo, en la zona horaria del negocio. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteService {

    public record Rango(LocalDate desde, LocalDate hasta) {}

    public record TotalMetodo(String metodo, long cantidad, BigDecimal total) {}

    public record Resumen(LocalDate desde, LocalDate hasta, long cantidadVentas, BigDecimal totalVentas,
            BigDecimal ticketPromedio, BigDecimal totalDescuentos, BigDecimal totalIva, long ventasAnuladas,
            List<TotalMetodo> porMetodo) {}

    public record VentaDia(LocalDate fecha, long cantidad, BigDecimal total) {}

    public record EstilistaTotal(Long estilistaId, String estilista, long servicios, BigDecimal total,
            BigDecimal comisionPorcentaje, BigDecimal comision) {}

    public record ItemTop(Long id, String nombre, long cantidad, BigDecimal total) {}

    /** Subtotal del ítem prorrateado con el descuento global de la venta. */
    private static final String MONTO_ITEM = "COALESCE(i.subtotal * v.total / NULLIF(v.subtotal, 0), 0)";

    private final JdbcClient jdbc;
    private final ZoneId zona;
    private final Clock clock;

    public Rango rango(LocalDate desde, LocalDate hasta) {
        LocalDate hoy = LocalDate.now(clock.withZone(zona));
        LocalDate d = desde != null ? desde : hoy;
        LocalDate h = hasta != null ? hasta : d;
        if (h.isBefore(d)) {
            throw new BusinessException("La fecha 'hasta' no puede ser anterior a 'desde'");
        }
        if (d.plusDays(366).isBefore(h)) {
            throw new BusinessException("El rango máximo es de un año");
        }
        return new Rango(d, h);
    }

    public Resumen resumen(Rango r) {
        var fila = jdbc.sql("""
                        SELECT COUNT(*) FILTER (WHERE estado = 'COMPLETADA')                       AS cantidad,
                               COALESCE(SUM(total) FILTER (WHERE estado = 'COMPLETADA'), 0)      AS total,
                               COALESCE(SUM(descuento) FILTER (WHERE estado = 'COMPLETADA'), 0)  AS descuentos,
                               COALESCE(SUM(iva) FILTER (WHERE estado = 'COMPLETADA'), 0)        AS iva,
                               COUNT(*) FILTER (WHERE estado = 'ANULADA')                         AS anuladas
                        FROM ventas
                        WHERE negocio_id = :nid AND fecha >= :desde AND fecha < :hasta
                        """)
                .params(params(r))
                .query((rs, n) -> new Object[] {rs.getLong("cantidad"), rs.getBigDecimal("total"),
                        rs.getBigDecimal("descuentos"), rs.getBigDecimal("iva"), rs.getLong("anuladas")})
                .single();
        long cantidad = (long) fila[0];
        BigDecimal total = (BigDecimal) fila[1];
        BigDecimal promedio = cantidad == 0 ? BigDecimal.ZERO.setScale(2)
                : total.divide(BigDecimal.valueOf(cantidad), 2, RoundingMode.HALF_UP);

        List<TotalMetodo> porMetodo = jdbc.sql("""
                        SELECT metodo_pago, COUNT(*) AS cantidad, SUM(total) AS total
                        FROM ventas
                        WHERE negocio_id = :nid AND estado = 'COMPLETADA' AND fecha >= :desde AND fecha < :hasta
                        GROUP BY metodo_pago
                        ORDER BY total DESC
                        """)
                .params(params(r))
                .query((rs, n) -> new TotalMetodo(rs.getString(1), rs.getLong(2), rs.getBigDecimal(3)))
                .list();

        return new Resumen(r.desde(), r.hasta(), cantidad, total, promedio, (BigDecimal) fila[2],
                (BigDecimal) fila[3], (long) fila[4], porMetodo);
    }

    public List<VentaDia> ventasPorDia(Rango r) {
        return jdbc.sql("""
                        SELECT CAST(fecha AT TIME ZONE :tz AS date) AS dia, COUNT(*) AS cantidad, SUM(total) AS total
                        FROM ventas
                        WHERE negocio_id = :nid AND estado = 'COMPLETADA' AND fecha >= :desde AND fecha < :hasta
                        GROUP BY dia
                        ORDER BY dia
                        """)
                .params(params(r))
                .param("tz", zona.getId())
                .query((rs, n) -> new VentaDia(rs.getDate(1).toLocalDate(), rs.getLong(2), rs.getBigDecimal(3)))
                .list();
    }

    public List<EstilistaTotal> porEstilista(Rango r, Long soloEstilistaId) {
        return jdbc.sql("""
                        SELECT u.id, u.nombre, SUM(i.cantidad) AS servicios,
                               ROUND(SUM(%s), 2) AS total, u.comision_porcentaje
                        FROM venta_items i
                        JOIN ventas v   ON v.id = i.venta_id
                        JOIN usuarios u ON u.id = i.estilista_id
                        WHERE v.negocio_id = :nid AND v.estado = 'COMPLETADA' AND v.fecha >= :desde AND v.fecha < :hasta
                          AND i.tipo = 'SERVICIO'
                          AND (CAST(:estilista AS BIGINT) IS NULL OR u.id = CAST(:estilista AS BIGINT))
                        GROUP BY u.id, u.nombre, u.comision_porcentaje
                        ORDER BY total DESC
                        """.formatted(MONTO_ITEM))
                .params(params(r))
                .param("estilista", soloEstilistaId, java.sql.Types.BIGINT)
                .query((rs, n) -> {
                    BigDecimal total = rs.getBigDecimal("total");
                    BigDecimal pct = rs.getBigDecimal("comision_porcentaje");
                    BigDecimal comision = total.multiply(pct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    return new EstilistaTotal(rs.getLong("id"), rs.getString("nombre"), rs.getLong("servicios"), total,
                            pct, comision);
                })
                .list();
    }

    public List<ItemTop> topItems(Rango r, String tipo, int limite) {
        String columna = "SERVICIO".equals(tipo) ? "servicio_id" : "producto_id";
        return jdbc.sql("""
                        SELECT i.%1$s AS id, MAX(i.descripcion) AS nombre, SUM(i.cantidad) AS cantidad,
                               ROUND(SUM(%2$s), 2) AS total
                        FROM venta_items i
                        JOIN ventas v ON v.id = i.venta_id
                        WHERE v.negocio_id = :nid AND v.estado = 'COMPLETADA' AND v.fecha >= :desde AND v.fecha < :hasta AND i.tipo = :tipo
                        GROUP BY i.%1$s
                        ORDER BY cantidad DESC, total DESC
                        LIMIT :limite
                        """.formatted(columna, MONTO_ITEM))
                .params(params(r))
                .param("tipo", tipo)
                .param("limite", Math.max(1, Math.min(limite, 50)))
                .query((rs, n) -> new ItemTop(rs.getLong("id"), rs.getString("nombre"), rs.getLong("cantidad"),
                        rs.getBigDecimal("total")))
                .list();
    }

    /** Exporta las ventas del rango (una fila por venta) en CSV separado por ';' (compatible con Excel en español). */
    public void exportarVentasCsv(Rango r, PrintWriter out) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(zona);
        out.print('﻿'); // BOM para que Excel detecte UTF-8
        out.println("id;fecha;cajero;cliente;subtotal;descuento;total;iva;metodo_pago;estado");
        jdbc.sql("""
                        SELECT v.id, v.fecha, u.nombre AS cajero, c.nombre AS cliente, v.subtotal, v.descuento,
                               v.total, v.iva, v.metodo_pago, v.estado
                        FROM ventas v
                        JOIN usuarios u ON u.id = v.cajero_id
                        LEFT JOIN clientes c ON c.id = v.cliente_id
                        WHERE v.negocio_id = :nid AND v.fecha >= :desde AND v.fecha < :hasta
                        ORDER BY v.fecha
                        """)
                .params(params(r))
                .query(rs -> {
                    out.println(String.join(";",
                            String.valueOf(rs.getLong("id")),
                            fmt.format(rs.getTimestamp("fecha").toInstant()),
                            csv(rs.getString("cajero")),
                            csv(rs.getString("cliente")),
                            rs.getBigDecimal("subtotal").toPlainString(),
                            rs.getBigDecimal("descuento").toPlainString(),
                            rs.getBigDecimal("total").toPlainString(),
                            rs.getBigDecimal("iva").toPlainString(),
                            rs.getString("metodo_pago"),
                            rs.getString("estado")));
                });
        out.flush();
    }

    private java.util.Map<String, Object> params(Rango r) {
        Instant desde = r.desde().atStartOfDay(zona).toInstant();
        Instant hasta = r.hasta().plusDays(1).atStartOfDay(zona).toInstant();
        // SQL nativo: Hibernate no aplica el filtro de tenant, se agrega negocio_id en cada consulta
        return java.util.Map.of("nid", TenantContext.actual(), "desde", java.sql.Timestamp.from(desde),
                "hasta", java.sql.Timestamp.from(hasta));
    }

    private static String csv(String valor) {
        if (valor == null) {
            return "";
        }
        String limpio = valor.replace("\"", "\"\"");
        return limpio.contains(";") || limpio.contains("\"") ? "\"" + limpio + "\"" : limpio;
    }
}
