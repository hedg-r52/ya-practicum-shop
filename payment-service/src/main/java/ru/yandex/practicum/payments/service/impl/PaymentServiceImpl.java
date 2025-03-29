package ru.yandex.practicum.payments.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payments.domain.BalanceResponse;
import ru.yandex.practicum.payments.domain.DepositRequest;
import ru.yandex.practicum.payments.domain.PaymentRequest;
import ru.yandex.practicum.payments.exception.NotEnoughMoneyException;
import ru.yandex.practicum.payments.exception.ResourceNotFoundException;
import ru.yandex.practicum.payments.model.BillingAccount;
import ru.yandex.practicum.payments.repository.BillingAccountRepository;
import ru.yandex.practicum.payments.service.PaymentService;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final BillingAccountRepository billingAccountRepository;

    @Override
    public Mono<BalanceResponse> getBalance() {
        return billingAccountRepository.findFirstByOrderByCreatedAt()
                .map(this::map);
    }

    @Override
    public Mono<BalanceResponse> processPayment(PaymentRequest request) {
        return billingAccountRepository.findFirstByOrderByCreatedAt()
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Аккаунт для платежа не найден")))
                .doOnNext(ba -> ba.setMoney(ba.getMoney().subtract(request.getValue())))
                .flatMap(ba -> {
                    if (ba.getMoney().compareTo(BigDecimal.ZERO) < 0) {
                        return Mono.error(new NotEnoughMoneyException("Недостаточно денег"));
                    }
                    return billingAccountRepository.save(ba);
                })
                .map(this::map);
    }

    @Override
    public Mono<BalanceResponse> depositMoney(DepositRequest request) {
        return billingAccountRepository.findFirstByOrderByCreatedAt()
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Аккаунт для платежа не найден")))
                .doOnNext(ba -> ba.setMoney(ba.getMoney().add(request.getValue())))
                .flatMap(billingAccountRepository::save)
                .map(this::map);
    }

    private BalanceResponse map(BillingAccount p) {
        var balanceResponse = new BalanceResponse();
        balanceResponse.setValue(p.getMoney());
        return balanceResponse;
    }
}
