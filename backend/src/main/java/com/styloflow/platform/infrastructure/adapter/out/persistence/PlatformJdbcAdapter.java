package com.styloflow.platform.infrastructure.adapter.out.persistence;

import com.styloflow.platform.application.port.out.PlatformRepositoryPort;
import com.styloflow.platform.domain.model.BaseCatalog;
import com.styloflow.platform.domain.model.BusinessSummary;
import com.styloflow.platform.domain.model.NewBusiness;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlatformJdbcAdapter implements PlatformRepositoryPort {

    private final JdbcClient jdbc;

    @Override
    public List<BusinessSummary> listSummaries() {
        return jdbc.sql("""
                        SELECT n.id, n.codigo, n.nombre, n.activo, n.created_at,
                               (SELECT COUNT(*) FROM usuarios u WHERE u.negocio_id = n.id) AS users,
                               COUNT(v.id) AS sales30d,
                               COALESCE(SUM(v.total), 0) AS total30d
                        FROM negocio n
                        LEFT JOIN ventas v ON v.negocio_id = n.id AND v.estado = 'COMPLETADA'
                                          AND v.fecha >= now() - INTERVAL '30 days'
                        GROUP BY n.id
                        ORDER BY n.created_at DESC
                        """)
                .query((rs, _) -> new BusinessSummary(
                        rs.getLong("id"),
                        rs.getString("codigo"),
                        rs.getString("nombre"),
                        rs.getBoolean("activo"),
                        rs.getTimestamp("created_at").toInstant(),
                        rs.getLong("users"),
                        rs.getLong("sales30d"),
                        rs.getBigDecimal("total30d"))
                )
                .list();
    }

    @Override
    public long register(NewBusiness business) {
        Long id = jdbc.sql("""
                        INSERT INTO negocio (codigo, nombre, nit, telefono, moneda, simbolo, iva_porcentaje, mensaje_ticket)
                        VALUES (:code, :name, :taxId, :phone, 'BOB', 'Bs', 13.00, '¡Gracias por su visita!')
                        RETURNING id
                        """)
                .param("code", business.code())
                .param("name", business.name())
                .param("taxId", business.taxId())
                .param("phone", business.phone())
                .query(Long.class)
                .single();
        jdbc.sql("""
                        INSERT INTO usuarios (negocio_id, nombre, username, password_hash, rol)
                        VALUES (:businessId, :name, :username, :hash, 'ADMIN')
                        """)
                .param("businessId", id)
                .param("name", business.adminName())
                .param("username", business.adminUsername())
                .param("hash", business.adminPasswordHash())
                .update();
        for (BaseCatalog.CategorySeed category : business.catalog()) {
            Long categoryId = jdbc.sql("INSERT INTO categorias (negocio_id, nombre) VALUES (:businessId, :name) RETURNING id")
                    .param("businessId", id)
                    .param("name", category.name())
                    .query(Long.class)
                    .single();
            for (BaseCatalog.ServiceSeed service : category.services()) {
                jdbc.sql("""
                                INSERT INTO servicios (negocio_id, categoria_id, nombre, duracion_min, precio)
                                VALUES (:businessId, :categoryId, :name, :duration, :price)
                                """)
                        .param("businessId", id)
                        .param("categoryId", categoryId)
                        .param("name", service.name())
                        .param("duration", service.durationMinutes())
                        .param("price", service.price())
                        .update();
            }
        }
        return id;
    }
}
