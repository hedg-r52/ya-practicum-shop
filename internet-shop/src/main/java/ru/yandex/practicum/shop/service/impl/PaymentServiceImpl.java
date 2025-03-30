package ru.yandex.practicum.shop.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.api.PaymentsApi;
import ru.yandex.practicum.shop.dto.BalanceResponse;
import ru.yandex.practicum.shop.dto.PaymentRequest;
import ru.yandex.practicum.shop.service.PaymentService;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentsApi paymentsApi;

    @Override
    public Mono<BigDecimal> getBalance() {
        return paymentsApi.paymentsGet()
                .onErrorResume(throwable -> Mono.empty())
                .onErrorComplete()
                .map(BalanceResponse::getValue);
    }

    @Override
    public Mono<BigDecimal> processPayment(BigDecimal value) {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setValue(value);
        return paymentsApi.paymentsPatch(paymentRequest)
                .map(balanceResponse -> balanceResponse.getValue().setScale(2, RoundingMode.HALF_UP));
    }
}
