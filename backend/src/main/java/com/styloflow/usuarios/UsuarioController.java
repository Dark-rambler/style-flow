package com.styloflow.usuarios;

import com.styloflow.auth.CurrentUser;
import com.styloflow.usuarios.UsuarioDtos.CambiarPasswordRequest;
import com.styloflow.usuarios.UsuarioDtos.UsuarioRequest;
import com.styloflow.usuarios.UsuarioDtos.UsuarioResponse;
import com.styloflow.usuarios.UsuarioDtos.UsuarioResumen;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService service;
    private final CurrentUser currentUser;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UsuarioResponse> listar() {
        return service.listar();
    }

    /** Estilistas activos, para asignarlos a servicios en el POS. */
    @GetMapping("/estilistas")
    public List<UsuarioResumen> estilistas() {
        return service.estilistas();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResponse crear(@Valid @RequestBody UsuarioRequest req) {
        return service.crear(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResponse actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest req) {
        return service.actualizar(id, req, currentUser.id());
    }

    @PutMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarMiPassword(@Valid @RequestBody CambiarPasswordRequest req) {
        service.cambiarPassword(currentUser.id(), req.password());
    }
}
