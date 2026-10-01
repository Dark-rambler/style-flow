package com.styloflow.auth;

import com.styloflow.usuarios.Usuario;
import com.styloflow.usuarios.UsuarioDtos.UsuarioResponse;
import com.styloflow.usuarios.UsuarioRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final CurrentUser currentUser;

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}

    public record LoginResponse(String token, Instant expiresAt, UsuarioResponse usuario) {}

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        Usuario u = usuarios.findByUsernameIgnoreCase(req.username().trim())
                .filter(Usuario::isActivo)
                .filter(x -> passwordEncoder.matches(req.password(), x.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos"));
        TokenService.Token token = tokenService.generar(u);
        return new LoginResponse(token.value(), token.expiresAt(), UsuarioResponse.from(u));
    }

    @GetMapping("/me")
    public UsuarioResponse me() {
        return usuarios.findById(currentUser.id())
                .filter(Usuario::isActivo)
                .map(UsuarioResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}
