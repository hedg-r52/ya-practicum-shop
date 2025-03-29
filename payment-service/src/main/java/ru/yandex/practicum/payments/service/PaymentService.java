package ru.yandex.practicum.payments.service;

import reactor.core.publisher.Mono;
import ru.yandex.practicum.payments.domain.BalanceResponse;
import ru.yandex.practicum.payments.domain.DepositRequest;
import ru.yandex.practicum.payments.domain.PaymentRequest;

public interface PaymentService {
    Mono<BalanceResponse> getBalance();

    Mono<BalanceResponse> processPayment(PaymentRequest request);

    Mono<BalanceResponse> depositMoney(DepositRequest request);
}
