package com.styloflow.negocio;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NegocioRepository extends JpaRepository<Negocio, Long> {

    Optional<Negocio> findByCodigoIgnoreCase(String codigo);
}
