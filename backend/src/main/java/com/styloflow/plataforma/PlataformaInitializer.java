package com.styloflow.plataforma;

import com.styloflow.config.AppProperties;
import com.styloflow.negocio.NegocioRepository;
import com.styloflow.tenant.TenantContext;
import com.styloflow.usuarios.Rol;
import com.styloflow.usuarios.Usuario;
import com.styloflow.usuarios.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Datos mínimos para arrancar: el superadmin de la plataforma y, si el negocio "demo" no tiene usuarios,
 * su administrador (útil en desarrollo y tests).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlataformaInitializer implements ApplicationRunner {

    static final String NEGOCIO_DEMO = "demo";

    private final SuperadminRepository superadmins;
    private final NegocioRepository negocios;
    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties props;

    @Override
    public void run(ApplicationArguments args) {
        if (superadmins.count() == 0) {
            Superadmin s = new Superadmin();
            s.setNombre(props.superadmin().nombre());
            s.setUsername(props.superadmin().username());
            s.setPasswordHash(passwordEncoder.encode(props.superadmin().password()));
            superadmins.save(s);
            log.warn("Superadmin de plataforma creado: '{}'. Cambie la contraseña (SUPERADMIN_PASSWORD).", s.getUsername());
        }

        negocios.findByCodigoIgnoreCase(NEGOCIO_DEMO).ifPresent(demo -> TenantContext.ejecutarComo(demo.getId(), () -> {
            if (usuarios.count() == 0) {
                Usuario admin = new Usuario();
                admin.setNombre(props.admin().nombre());
                admin.setUsername(props.admin().username());
                admin.setPasswordHash(passwordEncoder.encode(props.admin().password()));
                admin.setRol(Rol.ADMIN);
                usuarios.save(admin);
                log.warn("Administrador del negocio '{}' creado: '{}'.", NEGOCIO_DEMO, admin.getUsername());
            }
            return null;
        }));
    }
}
