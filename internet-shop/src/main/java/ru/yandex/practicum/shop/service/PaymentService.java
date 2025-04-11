package ru.yandex.practicum.shop.service;

import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface PaymentService {
    Mono<BigDecimal> getBalance(Long userId);

    Mono<BigDecimal> processPayment(Long userId, BigDecimal value);

    Mono<BigDecimal> depositPayment(Long userId, BigDecimal value);
}
