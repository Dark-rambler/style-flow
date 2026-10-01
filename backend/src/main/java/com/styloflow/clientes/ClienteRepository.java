package com.styloflow.clientes;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    @Query("""
            select c from Cliente c
            where :q = '' or lower(c.nombre) like lower(concat('%', :q, '%'))
               or c.telefono like concat('%', :q, '%')
               or lower(c.ciNit) like lower(concat('%', :q, '%'))
            """)
    Page<Cliente> buscar(String q, Pageable pageable);
}
