package com.styloflow.cashregister.application.service;

import com.styloflow.cashregister.application.port.in.CashRegisterUseCase;
import com.styloflow.cashregister.application.port.in.command.CloseCashRegisterCommand;
import com.styloflow.cashregister.application.port.in.command.OpenCashRegisterCommand;
import com.styloflow.cashregister.application.port.out.CashRegisterRepositoryPort;
import com.styloflow.cashregister.application.port.out.SalesTotalsPort;
import com.styloflow.cashregister.domain.exception.NoOpenCashRegisterException;
import com.styloflow.cashregister.domain.model.CashRegister;
import com.styloflow.cashregister.domain.model.CashRegisterSummary;
import com.styloflow.cashregister.domain.model.PaymentTotal;
import com.styloflow.shared.domain.exception.BusinessRuleException;
import com.styloflow.shared.domain.exception.NotFoundException;
import com.styloflow.shared.domain.model.PageResult;
import com.styloflow.users.application.port.out.UserRepositoryPort;
import com.styloflow.users.domain.model.User;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CashRegisterService implements CashRegisterUseCase {

    private final CashRegisterRepositoryPort cashRegisterRepository;
    private final SalesTotalsPort salesTotals;
    private final UserRepositoryPort userRepository;
    private final Clock clock;

    @Override
    public Optional<CashRegisterSummary> current() {
        return cashRegisterRepository.findOpen().map(this::summary);
    }

    @Override
    @Transactional
    public CashRegisterSummary open(OpenCashRegisterCommand command, Long userId) {
        if (cashRegisterRepository.findOpen().isPresent()) {
            throw new BusinessRuleException("A cash register is already open");
        }
        CashRegister cashRegister = CashRegister.open(
                getUserOrThrow(userId),
                clock.instant(),
                command.openingAmount(),
                command.notes()
        );
        return summary(cashRegisterRepository.save(cashRegister));
    }

    @Override
    @Transactional
    public CashRegisterSummary close(CloseCashRegisterCommand command, Long userId) {
        CashRegister cashRegister = cashRegisterRepository.findOpen().orElseThrow(NoOpenCashRegisterException::new);
        List<PaymentTotal> byPaymentMethod = salesTotals.totalsByPaymentMethod(cashRegister.getId());
        cashRegister.close(
                getUserOrThrow(userId),
                clock.instant(),
                command.countedCash(),
                command.notes(),
                byPaymentMethod
        );
        return CashRegisterSummary.of(cashRegisterRepository.save(cashRegister), byPaymentMethod);
    }

    @Override
    public PageResult<CashRegisterSummary> history(int page, int size) {
        return cashRegisterRepository.findHistory(page, Math.min(size, 100)).map(this::summary);
    }

    @Override
    public CashRegisterSummary get(Long id) {
        return cashRegisterRepository.findById(id).map(this::summary)
                .orElseThrow(() -> new NotFoundException("Cash register", id));
    }

    private CashRegisterSummary summary(CashRegister cashRegister) {
        return CashRegisterSummary.of(cashRegister, salesTotals.totalsByPaymentMethod(cashRegister.getId()));
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User", id));
    }
}
