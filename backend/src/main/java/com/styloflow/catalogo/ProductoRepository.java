package com.styloflow.catalogo;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findAllByOrderByNombreAsc();

    boolean existsBySkuIgnoreCase(String sku);

    /** Bloquea la fila para descontar stock sin condiciones de carrera. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p where p.id = :id")
    Optional<Producto> findByIdForUpdate(Long id);

    @Query("select p from Producto p where p.activo = true and p.stock <= p.stockMinimo order by p.stock")
    List<Producto> findStockBajo();
}
