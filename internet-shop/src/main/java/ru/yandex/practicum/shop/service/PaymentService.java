package ru.yandex.practicum.shop.service;

import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface PaymentService {
    Mono<BigDecimal> getBalance();

    Mono<BigDecimal> processPayment(BigDecimal value);
}
