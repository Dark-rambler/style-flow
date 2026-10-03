package com.styloflow.cash.infrastructure.adapter.out.persistence;

import com.styloflow.cash.application.port.out.CashRepositoryPort;
import com.styloflow.cash.domain.model.Cash;
import com.styloflow.cash.domain.model.CashStatus;
import com.styloflow.shared.domain.model.PageResult;
import com.styloflow.shared.infrastructure.persistence.PageResults;
import com.styloflow.users.infrastructure.adapter.out.persistence.UserJpaRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CashPersistenceAdapter implements CashRepositoryPort {

    private final CashJpaRepository cashRepository;
    private final UserJpaRepository userRepository;
    private final CashPersistenceMapper cashRegisterMapper;

    @Override
    public Optional<Cash> findOpen() {
        return cashRepository.findFirstByStatus(CashStatus.OPEN).map(cashRegisterMapper::toDomain);
    }

    @Override
    public Optional<Cash> findById(Long id) {
        return cashRepository.findById(id).map(cashRegisterMapper::toDomain);
    }

    @Override
    public PageResult<Cash> findHistory(int page, int size) {
        return PageResults.of(
                cashRepository.findAllByOrderByOpenedAtDesc(PageRequest.of(page, size)),
                cashRegisterMapper::toDomain
        );
    }

    @Override
    public Cash save(Cash cash) {
        CashEntity entity;
        if(cash.getId() == null){
            entity = new CashEntity();
            cashRegisterMapper.updateEntity(cash, entity);
            entity.setOpenedBy(userRepository.getReferenceById(cash.getOpenedBy().getId()));
        }else{
            entity = cashRepository.getReferenceById(cash.getId());
            entity.setClosedBy(userRepository.getReferenceById(cash.getClosedBy().getId()));
            cashRegisterMapper.updateEntity(cash, entity);
        }
        return cashRegisterMapper.toDomain(cashRepository.saveAndFlush(entity));
    }
}
