package com.styloflow.auth;

import com.styloflow.config.AppProperties;
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

    public Token generar(Usuario u) {
        Instant now = clock.instant();
        Instant exp = now.plus(Duration.ofHours(props.jwt().expirationHours()));
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(props.jwt().issuer())
                .issuedAt(now)
                .expiresAt(exp)
                .subject(u.getUsername())
                .claim(CLAIM_UID, u.getId())
                .claim("nombre", u.getNombre())
                .claim(CLAIM_ROLES, List.of(u.getRol().name()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String value = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new Token(value, exp);
    }
}
