package com.styloflow.auth.infrastructure.security;

import com.styloflow.auth.application.port.out.TokenPort;
import com.styloflow.auth.domain.model.AuthToken;
import com.styloflow.business.domain.model.Business;
import com.styloflow.platform.domain.model.Superadmin;
import com.styloflow.shared.infrastructure.config.AppProperties;
import com.styloflow.shared.infrastructure.tenant.TenantContext;
import com.styloflow.users.domain.model.User;
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

    static final String CLAIM_UID = "uid";
    static final String CLAIM_ROLES = "roles";

    private final JwtEncoder encoder;
    private final AppProperties props;
    private final Clock clock;

    @Override
    public AuthToken generate(User user, Business business) {
        return issue(base(user.getUsername(), user.getId(), user.getName(), user.getRole().name())
                .claim(TenantContext.CLAIM_BUSINESS_ID, business.getId())
                .claim(TenantContext.CLAIM_BUSINESS_CODE, business.getCode()));
    }

    @Override
    public AuthToken generatePlatform(Superadmin superadmin) {
        return issue(base(superadmin.getUsername(), superadmin.getId(), superadmin.getName(), "SUPERADMIN"));
    }

    private JwtClaimsSet.Builder base(String subject, Long uid, String name, String role) {
        return JwtClaimsSet.builder()
                .subject(subject)
                .claim(CLAIM_UID, uid)
                .claim("name", name)
                .claim(CLAIM_ROLES, List.of(role));
    }

    private AuthToken issue(JwtClaimsSet.Builder builder) {
        var now = clock.instant();
        var expiresAt = now.plus(Duration.ofHours(props.jwt().expirationHours()));
        var claims = builder.issuer(props.jwt().issuer()).issuedAt(now).expiresAt(expiresAt).build();
        var header = JwsHeader.with(MacAlgorithm.HS256).build();
        return new AuthToken(encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue(), expiresAt);
    }
}
