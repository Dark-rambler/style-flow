package com.styloflow.platform.infrastructure.adapter.out.persistence;

import com.styloflow.platform.application.port.out.SuperadminRepositoryPort;
import com.styloflow.platform.domain.model.SuperadminModel;
import com.styloflow.shared.domain.exception.NotFoundException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SuperadminPersistenceAdapter implements SuperadminRepositoryPort {

    private final SuperadminJpaRepository superadminRepository;
    private final SuperadminPersistenceMapper superadminMapper;

    @Override
    public Optional<SuperadminModel> findByUsername(String username) {
        return superadminRepository.findByUsernameIgnoreCase(username).map(superadminMapper::toDomain);
    }

    @Override
    public long count() {
        return superadminRepository.count();
    }

    @Override
    public SuperadminModel save(SuperadminModel superadmin) {
        var entity = superadmin.getId() == null ? new SuperadminEntity() :
                superadminRepository.findById(superadmin.getId())
                        .orElseThrow(() -> new NotFoundException("Superadmin", superadmin.getId()));
        superadminMapper.updateEntity(superadmin, entity);
        return superadminMapper.toDomain(superadminRepository.save(entity));
    }
}
