package com.styloflow.cash.application.service;

import com.styloflow.cash.application.port.in.CashUseCase;
import com.styloflow.cash.application.port.in.command.CloseCashCommand;
import com.styloflow.cash.application.port.in.command.OpenCashCommand;
import com.styloflow.cash.application.port.out.CashRepositoryPort;
import com.styloflow.cash.application.port.out.SalesTotalsPort;
import com.styloflow.cash.domain.exception.NoOpenCashException;
import com.styloflow.cash.domain.model.Cash;
import com.styloflow.cash.domain.model.CashSummary;
import com.styloflow.shared.domain.exception.BusinessRuleException;
import com.styloflow.shared.domain.exception.NotFoundException;
import com.styloflow.shared.domain.model.PageResult;
import com.styloflow.users.application.port.out.UserRepositoryPort;
import com.styloflow.users.domain.model.User;
import java.time.Clock;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CashService implements CashUseCase {

    private final CashRepositoryPort cashRepository;
    private final SalesTotalsPort salesTotals;
    private final UserRepositoryPort userRepository;
    private final Clock clock;

    @Override
    public Optional<CashSummary> current() {
        return cashRepository.findOpen().map(this::summary);
    }

    @Override
    @Transactional
    public CashSummary open(OpenCashCommand command, Long userId) {
        if (cashRepository.findOpen().isPresent())
            throw new BusinessRuleException("A cash register is already open");
        var cash = Cash.open(
                getUserOrThrow(userId),
                clock.instant(),
                command.openingAmount(),
                command.notes()
        );
        return summary(cashRepository.save(cash));
    }

    @Override
    @Transactional
    public CashSummary close(CloseCashCommand command, Long userId) {
        var cash = cashRepository.findOpen().orElseThrow(NoOpenCashException::new);
        var byPaymentMethod = salesTotals.totalsByPaymentMethod(cash.getId());
        cash.close(
                getUserOrThrow(userId),
                clock.instant(),
                command.countedCash(),
                command.notes(),
                byPaymentMethod
        );
        return CashSummary.of(cashRepository.save(cash), byPaymentMethod);
    }

    @Override
    public PageResult<CashSummary> history(int page, int size) {
        return cashRepository.findHistory(page, Math.min(size, 100))
                .map(this::summary);
    }

    @Override
    public CashSummary get(Long id) {
        return cashRepository.findById(id)
                .map(this::summary)
                .orElseThrow(() -> new NotFoundException("Cash register", id));
    }

    private CashSummary summary(Cash cash) {
        return CashSummary.of(cash, salesTotals.totalsByPaymentMethod(cash.getId()));
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User", id));
    }
}
