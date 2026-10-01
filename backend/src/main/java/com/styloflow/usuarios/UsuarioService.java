package com.styloflow.usuarios;

import com.styloflow.common.BusinessException;
import com.styloflow.common.NotFoundException;
import com.styloflow.usuarios.UsuarioDtos.UsuarioRequest;
import com.styloflow.usuarios.UsuarioDtos.UsuarioResponse;
import com.styloflow.usuarios.UsuarioDtos.UsuarioResumen;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repo;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return repo.findAllByOrderByNombreAsc().stream().map(UsuarioResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<UsuarioResumen> estilistas() {
        return repo.findByRolAndActivoTrueOrderByNombreAsc(Rol.ESTILISTA).stream().map(UsuarioResumen::from).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        return UsuarioResponse.from(buscar(id));
    }

    @Transactional
    public UsuarioResponse crear(UsuarioRequest req) {
        if (req.password() == null || req.password().isBlank()) {
            throw new BusinessException("La contraseña es obligatoria al crear un usuario");
        }
        if (repo.existsByUsernameIgnoreCase(req.username())) {
            throw new BusinessException("El nombre de usuario ya existe");
        }
        Usuario u = new Usuario();
        aplicar(u, req);
        u.setPasswordHash(passwordEncoder.encode(req.password()));
        return UsuarioResponse.from(repo.save(u));
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioRequest req, Long usuarioActualId) {
        Usuario u = buscar(id);
        if (!u.getUsername().equalsIgnoreCase(req.username()) && repo.existsByUsernameIgnoreCase(req.username())) {
            throw new BusinessException("El nombre de usuario ya existe");
        }
        boolean dejaDeSerAdminActivo = u.getRol() == Rol.ADMIN && u.isActivo()
                && (req.rol() != Rol.ADMIN || Boolean.FALSE.equals(req.activo()));
        if (dejaDeSerAdminActivo && repo.countByRolAndActivoTrue(Rol.ADMIN) <= 1) {
            throw new BusinessException("Debe existir al menos un administrador activo");
        }
        if (id.equals(usuarioActualId) && Boolean.FALSE.equals(req.activo())) {
            throw new BusinessException("No puede desactivar su propio usuario");
        }
        aplicar(u, req);
        if (req.password() != null && !req.password().isBlank()) {
            u.setPasswordHash(passwordEncoder.encode(req.password()));
        }
        return UsuarioResponse.from(u);
    }

    @Transactional
    public void cambiarPassword(Long id, String password) {
        buscar(id).setPasswordHash(passwordEncoder.encode(password));
    }

    Usuario buscar(Long id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Usuario", id));
    }

    private static void aplicar(Usuario u, UsuarioRequest req) {
        u.setNombre(req.nombre().trim());
        u.setUsername(req.username().trim().toLowerCase());
        u.setRol(req.rol());
        u.setTelefono(req.telefono());
        u.setComisionPorcentaje(req.comisionPorcentaje() != null ? req.comisionPorcentaje() : BigDecimal.ZERO);
        if (req.activo() != null) {
            u.setActivo(req.activo());
        }
    }
}
