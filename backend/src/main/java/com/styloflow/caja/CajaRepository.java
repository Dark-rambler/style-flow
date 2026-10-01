package com.styloflow.caja;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CajaRepository extends JpaRepository<Caja, Long> {

    @EntityGraph(attributePaths = {"abiertaPor", "cerradaPor"})
    Optional<Caja> findFirstByEstado(Caja.Estado estado);

    @EntityGraph(attributePaths = {"abiertaPor", "cerradaPor"})
    Page<Caja> findAllByOrderByAbiertaEnDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"abiertaPor", "cerradaPor"})
    Optional<Caja> findWithUsuariosById(Long id);
}
