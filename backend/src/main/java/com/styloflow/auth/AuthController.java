package com.styloflow.auth;

import com.styloflow.negocio.Negocio;
import com.styloflow.negocio.NegocioRepository;
import com.styloflow.tenant.TenantContext;
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
    private final NegocioRepository negocios;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final CurrentUser currentUser;

    public record LoginRequest(@NotBlank String negocio, @NotBlank String username, @NotBlank String password) {}

    public record NegocioInfo(String codigo, String nombre) {}

    public record LoginResponse(String token, Instant expiresAt, UsuarioResponse usuario, NegocioInfo negocio) {}

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        // Mismo mensaje para cualquier dato incorrecto: no revela qué negocios o usuarios existen
        ResponseStatusException credencialesInvalidas =
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Negocio, usuario o contraseña incorrectos");
        Negocio n = negocios.findByCodigoIgnoreCase(req.negocio().trim()).orElseThrow(() -> credencialesInvalidas);
        // El usuario se busca dentro de su negocio (el JWT todavía no existe)
        Usuario u = TenantContext.ejecutarComo(n.getId(), () -> usuarios.findByUsernameIgnoreCase(req.username().trim()))
                .filter(Usuario::isActivo)
                .filter(x -> passwordEncoder.matches(req.password(), x.getPasswordHash()))
                .orElseThrow(() -> credencialesInvalidas);
        if (!n.isActivo()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este negocio está suspendido. Contacte al soporte.");
        }
        TokenService.Token token = tokenService.generar(u, n);
        return new LoginResponse(token.value(), token.expiresAt(), UsuarioResponse.from(u),
                new NegocioInfo(n.getCodigo(), n.getNombre()));
    }

    @GetMapping("/me")
    public UsuarioResponse me() {
        return usuarios.findById(currentUser.id())
                .filter(Usuario::isActivo)
                .map(UsuarioResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}
