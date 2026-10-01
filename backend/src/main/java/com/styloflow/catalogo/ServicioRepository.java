package com.styloflow.catalogo;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    @EntityGraph(attributePaths = "categoria")
    @Query("select s from Servicio s order by s.categoria.nombre, s.nombre")
    List<Servicio> findAllConCategoria();

    long countByCategoriaId(Long categoriaId);
}
