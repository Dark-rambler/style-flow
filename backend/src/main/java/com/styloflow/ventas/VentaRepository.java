package com.styloflow.ventas;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface VentaRepository extends JpaRepository<Venta, Long> {

    /** Total y cantidad de ventas completadas de una caja, agrupadas por método de pago. */
    interface TotalMetodo {
        MetodoPago getMetodoPago();

        long getCantidad();

        java.math.BigDecimal getTotal();
    }

    @Query("""
            select v.metodoPago as metodoPago, count(v) as cantidad, sum(v.total) as total
            from Venta v
            where v.caja.id = :cajaId and v.estado = com.styloflow.ventas.Venta.Estado.COMPLETADA
            group by v.metodoPago
            """)
    List<TotalMetodo> totalesPorMetodo(Long cajaId);

    @EntityGraph(attributePaths = {"cajero", "cliente", "caja"})
    @Query("""
            select v from Venta v
            where v.fecha >= :desde and v.fecha < :hasta
              and (:clienteId is null or v.cliente.id = :clienteId)
            order by v.fecha desc
            """)
    Page<Venta> buscar(Instant desde, Instant hasta, Long clienteId, Pageable pageable);

    @EntityGraph(attributePaths = {"cajero", "cliente", "anuladaPor", "items", "items.estilista"})
    @Query("select v from Venta v where v.id = :id")
    Optional<Venta> findDetalle(Long id);
}
