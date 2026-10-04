package com.styloflow.auth.infrastructure.security;

import com.styloflow.auth.application.port.out.TokenPort;
import com.styloflow.auth.domain.model.AuthToken;
import com.styloflow.business.domain.model.BusinessModel;
import com.styloflow.platform.domain.model.SuperadminModel;
import com.styloflow.shared.infrastructure.config.AppProperties;
import com.styloflow.shared.infrastructure.tenant.TenantContext;
import com.styloflow.users.domain.model.UserModel;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenAdapter implements TokenPort {

    private final JwtEncoder encoder;
    private final AppProperties props;
    private final Clock clock;

    @Override
    public AuthToken generate(UserModel user, BusinessModel business) {
        return issue(base(user.getId(), user.getUsername(), user.getName(), user.getRole().name())
                .claim(TenantContext.CLAIM_BUSINESS_ID, business.getId())
                .claim(TenantContext.CLAIM_BUSINESS_CODE, business.getCode()));
    }

    @Override
    public AuthToken generatePlatform(SuperadminModel superadmin) {
        return issue(base(superadmin.getId(), superadmin.getUsername(), superadmin.getName(), "SUPERADMIN"));
    }

    private JwtClaimsSet.Builder base(Long subject, String username, String name, String role) {
        return JwtClaimsSet.builder()
                .subject(subject.toString())
                .claim("username", username)
                .claim("name", name)
                .claim("roles", List.of(role));
    }

    private AuthToken issue(JwtClaimsSet.Builder builder) {
        var now = clock.instant();
        var expiresAt = now.plus(Duration.ofHours(props.jwt().expirationHours()));
        var claims = builder.issuer(props.jwt().issuer()).issuedAt(now).expiresAt(expiresAt).build();
        var header = JwsHeader.with(MacAlgorithm.HS256).build();
        return new AuthToken(encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue(), expiresAt);
    }
}
