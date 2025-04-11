package ru.yandex.practicum.payments.service;

import reactor.core.publisher.Mono;
import ru.yandex.practicum.payments.domain.BalanceResponse;
import ru.yandex.practicum.payments.domain.DepositRequest;
import ru.yandex.practicum.payments.domain.PaymentRequest;

public interface PaymentService {
    Mono<BalanceResponse> createAccount(Long userId);

    Mono<BalanceResponse> getBalance(Long userId);

    Mono<BalanceResponse> processPayment(Long userId, PaymentRequest request);

    Mono<BalanceResponse> depositMoney(Long userId, DepositRequest request);
}
