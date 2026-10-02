package com.styloflow.shared.application.port.out;

/** Business (tenant) the current request runs in. */
public interface CurrentTenantPort {

    long businessId();
}
