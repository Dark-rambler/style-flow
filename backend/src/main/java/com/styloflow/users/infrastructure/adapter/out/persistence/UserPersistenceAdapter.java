package com.styloflow.users.infrastructure.adapter.out.persistence;

import com.styloflow.shared.domain.exception.NotFoundException;
import com.styloflow.shared.infrastructure.tenant.TenantExecutor;
import com.styloflow.users.application.port.out.UserRepositoryPort;
import com.styloflow.users.domain.enums.Role;
import com.styloflow.users.domain.model.UserModel;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final UserJpaRepository userRepository;
    private final UserPersistenceMapper userMapper;
    private final TenantExecutor tenantExecutor;

    @Override
    public Optional<UserModel> findById(Long id) {
        return userRepository.findById(id).map(userMapper::toDomain);
    }

    @Override
    public Optional<UserModel> findByUsernameInBusiness(long businessId, String username) {
        return tenantExecutor.runAs(businessId,
                () -> userRepository.findByUsernameIgnoreCase(username).map(userMapper::toDomain));
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsernameIgnoreCase(username);
    }

    @Override
    public List<UserModel> findAllSorted() {
        return userMapper.toDomainList(userRepository.findAllByOrderByNameAsc());
    }

    @Override
    public List<UserModel> findActiveByRole(Role role) {
        return userMapper.toDomainList(userRepository.findByRoleAndActiveTrueOrderByNameAsc(role));
    }

    @Override
    public long countActiveByRole(Role role) {
        return userRepository.countByRoleAndActiveTrue(role);
    }

    @Override
    public long count() {
        return userRepository.count();
    }

    @Override
    public UserModel save(UserModel user) {
        UserEntity entity = user.getId() == null ? new UserEntity() :
                userRepository.findById(user.getId())
                        .orElseThrow(() -> new NotFoundException("User", user.getId()));
        userMapper.updateEntity(user, entity);
        return userMapper.toDomain(userRepository.save(entity));
    }
}
