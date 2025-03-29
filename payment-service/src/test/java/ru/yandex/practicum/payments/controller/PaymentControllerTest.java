package ru.yandex.practicum.payments.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payments.domain.BalanceResponse;
import ru.yandex.practicum.payments.domain.DepositRequest;
import ru.yandex.practicum.payments.domain.PaymentRequest;
import ru.yandex.practicum.payments.service.PaymentService;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    void whenGetBalance_shouldReturnPositiveBalance() {
        var balanceResponse = new BalanceResponse();
        balanceResponse.setValue(BigDecimal.valueOf(100.00));

        when(paymentService.getBalance())
                .thenReturn(Mono.just(balanceResponse));

        webTestClient.get()
                .uri("/payments")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.value").isEqualTo(100.00);
    }

    @Test
    void whenProcessPayments_shouldProcessPaymentSuccessfully() {
        var balanceResponse = new BalanceResponse();
        balanceResponse.setValue(BigDecimal.valueOf(500));

        var request = new PaymentRequest();
        request.setValue(BigDecimal.valueOf(1000));

        when(paymentService.processPayment(request))
                .thenReturn(Mono.just(balanceResponse));

        webTestClient.patch()
                .uri("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.value").isEqualTo(500);

        verify(paymentService, times(1))
                .processPayment(any(PaymentRequest.class));
    }

    @Test
    void whenDepositChanges_thenBalanceShouldBeChanged() {
        var balanceResponse = new BalanceResponse();
        balanceResponse.setValue(BigDecimal.valueOf(2000));
        var request = new DepositRequest();
        request.setValue(BigDecimal.valueOf(1500));

        when(paymentService.depositMoney(request))
                .thenReturn(Mono.just(balanceResponse));

        webTestClient.post()
                .uri("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.value").isEqualTo(2000);

        verify(paymentService, times(1))
                .depositMoney(any(DepositRequest.class));
    }

}