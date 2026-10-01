package com.styloflow.auth;

import com.styloflow.config.AppProperties;
import com.styloflow.negocio.Negocio;
import com.styloflow.plataforma.Superadmin;
import com.styloflow.tenant.TenantContext;
import com.styloflow.usuarios.Usuario;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    static final String CLAIM_UID = "uid";
    static final String CLAIM_ROLES = "roles";

    private final JwtEncoder encoder;
    private final AppProperties props;
    private final Clock clock;

    public record Token(String value, Instant expiresAt) {}

    /** Token de un usuario dentro de su negocio: lleva el tenant en {@code nid}. */
    public Token generar(Usuario u, Negocio n) {
        return emitir(base(u.getUsername(), u.getId(), u.getNombre(), u.getRol().name())
                .claim(TenantContext.CLAIM_NEGOCIO_ID, n.getId())
                .claim(TenantContext.CLAIM_NEGOCIO_CODIGO, n.getCodigo()));
    }

    /** Token de plataforma: sin negocio, solo puede usar /api/plataforma/**. */
    public Token generarPlataforma(Superadmin s) {
        return emitir(base(s.getUsername(), s.getId(), s.getNombre(), "SUPERADMIN"));
    }

    private JwtClaimsSet.Builder base(String subject, Long uid, String nombre, String rol) {
        return JwtClaimsSet.builder()
                .subject(subject)
                .claim(CLAIM_UID, uid)
                .claim("nombre", nombre)
                .claim(CLAIM_ROLES, List.of(rol));
    }

    private Token emitir(JwtClaimsSet.Builder builder) {
        Instant now = clock.instant();
        Instant exp = now.plus(Duration.ofHours(props.jwt().expirationHours()));
        JwtClaimsSet claims = builder.issuer(props.jwt().issuer()).issuedAt(now).expiresAt(exp).build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String value = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new Token(value, exp);
    }
}
