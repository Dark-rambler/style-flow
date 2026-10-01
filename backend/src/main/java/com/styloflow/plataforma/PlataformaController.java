package com.styloflow.plataforma;

import com.styloflow.auth.TokenService;
import com.styloflow.plataforma.PlataformaService.EstadoRequest;
import com.styloflow.plataforma.PlataformaService.NegocioRequest;
import com.styloflow.plataforma.PlataformaService.NegocioResumen;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** API de la plataforma. Todo excepto el login exige rol SUPERADMIN (ver SecurityConfig). */
@RestController
@RequestMapping("/api/plataforma")
@RequiredArgsConstructor
public class PlataformaController {

    private final PlataformaService service;
    private final SuperadminRepository superadmins;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}

    public record LoginResponse(String token, Instant expiresAt, String nombre) {}

    @PostMapping("/auth/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        Superadmin s = superadmins.findByUsernameIgnoreCase(req.username().trim())
                .filter(Superadmin::isActivo)
                .filter(x -> passwordEncoder.matches(req.password(), x.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos"));
        TokenService.Token token = tokenService.generarPlataforma(s);
        return new LoginResponse(token.value(), token.expiresAt(), s.getNombre());
    }

    @GetMapping("/negocios")
    public List<NegocioResumen> negocios() {
        return service.listar();
    }

    @PostMapping("/negocios")
    @ResponseStatus(HttpStatus.CREATED)
    public NegocioResumen crear(@Valid @RequestBody NegocioRequest req) {
        return service.crear(req);
    }

    @PutMapping("/negocios/{id}/estado")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoRequest req) {
        service.cambiarEstado(id, req.activo());
    }
}
