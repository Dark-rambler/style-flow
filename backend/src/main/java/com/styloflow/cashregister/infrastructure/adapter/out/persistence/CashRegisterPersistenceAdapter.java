package com.styloflow.cashregister.infrastructure.adapter.out.persistence;

import com.styloflow.cashregister.application.port.out.CashRegisterRepositoryPort;
import com.styloflow.cashregister.domain.model.CashRegister;
import com.styloflow.cashregister.domain.model.CashRegisterStatus;
import com.styloflow.shared.domain.exception.NotFoundException;
import com.styloflow.shared.domain.model.PageResult;
import com.styloflow.shared.infrastructure.persistence.PageResults;
import com.styloflow.users.infrastructure.adapter.out.persistence.UserJpaRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CashRegisterPersistenceAdapter implements CashRegisterRepositoryPort {

    private final CashRegisterJpaRepository cashRegisterRepository;
    private final UserJpaRepository userRepository;
    private final CashRegisterPersistenceMapper cashRegisterMapper;

    @Override
    public Optional<CashRegister> findOpen() {
        return cashRegisterRepository.findFirstByStatus(CashRegisterStatus.OPEN).map(cashRegisterMapper::toDomain);
    }

    @Override
    public Optional<CashRegister> findById(Long id) {
        return cashRegisterRepository.findWithUsersById(id).map(cashRegisterMapper::toDomain);
    }

    @Override
    public PageResult<CashRegister> findHistory(int page, int size) {
        return PageResults.of(cashRegisterRepository.findAllByOrderByOpenedAtDesc(PageRequest.of(page, size)),
                cashRegisterMapper::toDomain);
    }

    @Override
    public CashRegister save(CashRegister cashRegister) {
        CashRegisterEntity entity = cashRegister.getId() == null ?
                new CashRegisterEntity() :
                cashRegisterRepository.findById(cashRegister.getId())
                        .orElseThrow(() -> new NotFoundException("Cash register", cashRegister.getId()));
        cashRegisterMapper.updateEntity(cashRegister, entity);
        entity.setOpenedBy(userRepository.getReferenceById(cashRegister.getOpenedBy().getId()));
        entity.setClosedBy(cashRegister.getClosedBy() != null ?
                userRepository.getReferenceById(cashRegister.getClosedBy().getId()) :
                null);
        return cashRegisterMapper.toDomain(cashRegisterRepository.saveAndFlush(entity));
    }
}
