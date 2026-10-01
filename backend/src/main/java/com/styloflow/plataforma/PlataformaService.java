package com.styloflow.plataforma;

import com.styloflow.common.BusinessException;
import com.styloflow.common.NotFoundException;
import com.styloflow.negocio.Negocio;
import com.styloflow.negocio.NegocioRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Administración de negocios (tenants) por el superadmin. */
@Service
@RequiredArgsConstructor
public class PlataformaService {

    public record NegocioRequest(
            @NotBlank @Size(max = 120) String nombre,
            @NotBlank @Pattern(regexp = "^[a-z0-9-]{3,40}$",
                    message = "3 a 40 caracteres: minúsculas, números y guiones") String codigo,
            @Size(max = 30) String nit,
            @Size(max = 30) String telefono,
            @NotBlank @Size(max = 120) String adminNombre,
            @NotBlank @Size(min = 3, max = 50) @Pattern(regexp = "^[a-zA-Z0-9._-]+$",
                    message = "solo letras, números, punto, guion y guion bajo") String adminUsername,
            @NotBlank @Size(min = 6, max = 72) String adminPassword,
            boolean catalogoBase) {}

    public record EstadoRequest(@NotNull Boolean activo) {}

    public record NegocioResumen(Long id, String codigo, String nombre, boolean activo, Instant createdAt,
            long usuarios, long ventas30d, BigDecimal total30d) {}

    private final JdbcClient jdbc;
    private final NegocioRepository negocios;
    private final NegocioEstadoService estado;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<NegocioResumen> listar() {
        return jdbc.sql("""
                        SELECT n.id, n.codigo, n.nombre, n.activo, n.created_at,
                               (SELECT COUNT(*) FROM usuarios u WHERE u.negocio_id = n.id) AS usuarios,
                               COUNT(v.id) AS ventas30d,
                               COALESCE(SUM(v.total), 0) AS total30d
                        FROM negocio n
                        LEFT JOIN ventas v ON v.negocio_id = n.id AND v.estado = 'COMPLETADA'
                                          AND v.fecha >= now() - INTERVAL '30 days'
                        GROUP BY n.id
                        ORDER BY n.created_at DESC
                        """)
                .query((rs, i) -> new NegocioResumen(rs.getLong("id"), rs.getString("codigo"), rs.getString("nombre"),
                        rs.getBoolean("activo"), rs.getTimestamp("created_at").toInstant(), rs.getLong("usuarios"),
                        rs.getLong("ventas30d"), rs.getBigDecimal("total30d")))
                .list();
    }

    /**
     * Alta atómica de un negocio con su administrador y, opcionalmente, el catálogo base.
     * Se hace con SQL directo (una sola transacción) porque el negocio todavía no existe como tenant.
     */
    @Transactional
    public NegocioResumen crear(NegocioRequest req) {
        String codigo = req.codigo().trim().toLowerCase();
        if (negocios.findByCodigoIgnoreCase(codigo).isPresent()) {
            throw new BusinessException("Ya existe un negocio con el código '" + codigo + "'");
        }
        Long id = jdbc.sql("""
                        INSERT INTO negocio (codigo, nombre, nit, telefono, moneda, simbolo, iva_porcentaje, mensaje_ticket)
                        VALUES (:codigo, :nombre, :nit, :telefono, 'BOB', 'Bs', 13.00, '¡Gracias por su visita!')
                        RETURNING id
                        """)
                .param("codigo", codigo)
                .param("nombre", req.nombre().trim())
                .param("nit", blankToNull(req.nit()))
                .param("telefono", blankToNull(req.telefono()))
                .query(Long.class)
                .single();

        jdbc.sql("""
                        INSERT INTO usuarios (negocio_id, nombre, username, password_hash, rol)
                        VALUES (:nid, :nombre, :username, :hash, 'ADMIN')
                        """)
                .param("nid", id)
                .param("nombre", req.adminNombre().trim())
                .param("username", req.adminUsername().trim().toLowerCase())
                .param("hash", passwordEncoder.encode(req.adminPassword()))
                .update();

        if (req.catalogoBase()) {
            for (CatalogoBase.CategoriaBase c : CatalogoBase.CATEGORIAS) {
                Long categoriaId = jdbc.sql("INSERT INTO categorias (negocio_id, nombre) VALUES (:nid, :nombre) RETURNING id")
                        .param("nid", id)
                        .param("nombre", c.nombre())
                        .query(Long.class)
                        .single();
                for (CatalogoBase.ServicioBase s : c.servicios()) {
                    jdbc.sql("""
                                    INSERT INTO servicios (negocio_id, categoria_id, nombre, duracion_min, precio)
                                    VALUES (:nid, :cat, :nombre, :duracion, :precio)
                                    """)
                            .param("nid", id)
                            .param("cat", categoriaId)
                            .param("nombre", s.nombre())
                            .param("duracion", s.duracionMin())
                            .param("precio", s.precio())
                            .update();
                }
            }
        }
        return listar().stream().filter(n -> n.id().equals(id)).findFirst().orElseThrow();
    }

    /** Sin transacción envolvente: primero se confirma el cambio y recién después se invalida la caché. */
    public void cambiarEstado(Long id, boolean activo) {
        Negocio n = negocios.findById(id).orElseThrow(() -> new NotFoundException("Negocio", id));
        n.setActivo(activo);
        negocios.save(n);
        estado.invalidar(id);
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
