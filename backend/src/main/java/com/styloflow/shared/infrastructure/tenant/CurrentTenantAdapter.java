package com.styloflow.shared.infrastructure.tenant;

import com.styloflow.shared.application.port.out.CurrentTenantPort;
import org.springframework.stereotype.Component;

@Component
public class CurrentTenantAdapter implements CurrentTenantPort {

    @Override
    public long businessId() {
        return TenantContext.current();
    }
}
