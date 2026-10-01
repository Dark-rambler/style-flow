package com.styloflow.usuarios;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);

    List<Usuario> findAllByOrderByNombreAsc();

    List<Usuario> findByRolAndActivoTrueOrderByNombreAsc(Rol rol);

    long countByRolAndActivoTrue(Rol rol);
}
