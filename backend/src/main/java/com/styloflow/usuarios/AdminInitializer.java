package com.styloflow.usuarios;

import com.styloflow.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Crea el administrador inicial si la base no tiene usuarios. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

    private final UsuarioRepository repo;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties props;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (repo.count() > 0) {
            return;
        }
        Usuario admin = new Usuario();
        admin.setNombre(props.admin().nombre());
        admin.setUsername(props.admin().username());
        admin.setPasswordHash(passwordEncoder.encode(props.admin().password()));
        admin.setRol(Rol.ADMIN);
        repo.save(admin);
        log.warn("Usuario administrador inicial creado: '{}'. Cambie la contraseña tras el primer ingreso.",
                admin.getUsername());
    }
}
