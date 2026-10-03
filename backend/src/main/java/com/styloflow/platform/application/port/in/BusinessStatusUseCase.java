package com.styloflow.platform.application.port.in;

public interface BusinessStatusUseCase {

    boolean isActive(Long businessId);

    void evict(Long businessId);
}
