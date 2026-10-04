package com.styloflow.shared.infrastructure.tenant;

import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class TenantExecutor {

    private final TransactionTemplate tx;

    public TenantExecutor(PlatformTransactionManager transactionManager) {
        this.tx = new TransactionTemplate(transactionManager);
        this.tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public <T> T runAs(long businessId, Supplier<T> action) {
        return TenantContext.runAs(businessId, () -> tx.execute(status -> action.get()));
    }
}
