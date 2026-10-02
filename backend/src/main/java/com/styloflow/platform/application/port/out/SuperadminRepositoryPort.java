package com.styloflow.platform.application.port.out;

import com.styloflow.platform.domain.model.Superadmin;
import java.util.Optional;

/** Persistence of platform superadmins (not tenant-scoped). */
public interface SuperadminRepositoryPort {

    /**
     * @param username login name
     * @return the superadmin, or empty
     */
    Optional<Superadmin> findByUsername(String username);

    /** @return number of superadmins */
    long count();

    /**
     * @param superadmin superadmin to persist
     * @return the persisted superadmin
     */
    Superadmin save(Superadmin superadmin);
}
