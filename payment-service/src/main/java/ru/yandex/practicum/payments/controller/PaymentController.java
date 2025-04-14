package ru.yandex.practicum.payments.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payments.api.PaymentsApi;
import ru.yandex.practicum.payments.domain.BalanceResponse;
import ru.yandex.practicum.payments.domain.DepositRequest;
import ru.yandex.practicum.payments.domain.PaymentRequest;
import ru.yandex.practicum.payments.service.PaymentService;

@RestController
@RequiredArgsConstructor
public class PaymentController implements PaymentsApi {

    private final PaymentService paymentService;

    @Override
    public Mono<BalanceResponse> paymentsBalanceUserIdGet(Long userId, ServerWebExchange exchange) {
        return paymentService.getBalance(userId);
    }

    @Override
    public Mono<BalanceResponse> paymentsWithdrawUserIdPost(Long userId, Mono<PaymentRequest> paymentRequest, ServerWebExchange exchange) {
        return paymentRequest
                .flatMap(request -> paymentService.processPayment(userId, request));
    }

    @Override
    public Mono<BalanceResponse> paymentsRefillUserIdPost(Long userId, Mono<DepositRequest> depositRequest, ServerWebExchange exchange) {
        return depositRequest
                .flatMap(request -> paymentService.depositMoney(userId, request));
    }

    @Override
    public Mono<BalanceResponse> paymentsCreateUserIdPost(Long userId, ServerWebExchange exchange) {
        return paymentService.createAccount(userId);
    }
}
