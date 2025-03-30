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
    public Mono<BalanceResponse> paymentsGet(ServerWebExchange exchange) {
        return paymentService.getBalance();
    }

    @Override
    public Mono<BalanceResponse> paymentsPatch(Mono<PaymentRequest> paymentRequest, ServerWebExchange exchange) {
        return paymentRequest
                .flatMap(paymentService::processPayment);
    }

    @Override
    public Mono<BalanceResponse> paymentsPost(Mono<DepositRequest> depositRequest, ServerWebExchange exchange) {
        return depositRequest
                .flatMap(paymentService::depositMoney);
    }
}
