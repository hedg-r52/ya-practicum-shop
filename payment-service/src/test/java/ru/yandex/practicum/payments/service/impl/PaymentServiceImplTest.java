package ru.yandex.practicum.payments.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.payments.domain.PaymentRequest;
import ru.yandex.practicum.payments.exception.NotEnoughMoneyException;
import ru.yandex.practicum.payments.model.BillingAccount;
import ru.yandex.practicum.payments.repository.BillingAccountRepository;
import ru.yandex.practicum.payments.service.PaymentService;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {PaymentServiceImpl.class})
class PaymentServiceImplTest {

    @Autowired
    private PaymentService paymentService;

    @MockitoBean
    private BillingAccountRepository billingAccountRepository;

    @BeforeEach
    void setUp() {
        reset(billingAccountRepository);
    }

    @Test
    void whenGetBalance_thenShouldReturnBalance() {
        when(billingAccountRepository.findFirstByOrderByCreatedAt())
                .thenReturn(Mono.just(BillingAccount.builder()
                        .id(1L)
                        .money(BigDecimal.valueOf(1000))
                        .createdAt(LocalDate.now())
                        .modifiedAt(LocalDate.now())
                        .build()));

        StepVerifier.create(paymentService.getBalance())
                .expectNextMatches(balanceResponse -> balanceResponse.getValue() != null
                        && balanceResponse.getValue().equals(BigDecimal.valueOf(1000)))
                .verifyComplete();
    }

    @Test
    void whenProcessPayment_thenShouldProcessPayment() {
        var request = new PaymentRequest();
        request.setValue(BigDecimal.valueOf(900));

        when(billingAccountRepository.findFirstByOrderByCreatedAt())
                .thenReturn(Mono.just(BillingAccount.builder()
                        .id(1L)
                        .money(BigDecimal.valueOf(1000))
                        .createdAt(LocalDate.now())
                        .modifiedAt(LocalDate.now())
                        .build()));

        when(billingAccountRepository.save(any(BillingAccount.class)))
                .thenReturn(Mono.just(BillingAccount.builder()
                        .id(1L)
                        .money(BigDecimal.valueOf(100))
                        .createdAt(LocalDate.now())
                        .modifiedAt(LocalDate.now())
                        .build()));

        StepVerifier.create(paymentService.processPayment(request))
                .expectNextMatches(balanceResponse -> balanceResponse.getValue() != null
                        && balanceResponse.getValue().equals(BigDecimal.valueOf(100)))
                .verifyComplete();

        verify(billingAccountRepository, times(1)).findFirstByOrderByCreatedAt();
        verify(billingAccountRepository, times(1)).save(any(BillingAccount.class));
    }

    @Test
    void whenProcessPaymentAndNotEnoughMoney_thenShouldThrowNotEnoughMoneyException() {
        var request = new PaymentRequest();
        request.setValue(BigDecimal.valueOf(1500));

        when(billingAccountRepository.findFirstByOrderByCreatedAt())
                .thenReturn(Mono.just(BillingAccount.builder()
                        .id(1L)
                        .money(BigDecimal.valueOf(1000))
                        .createdAt(LocalDate.now())
                        .modifiedAt(LocalDate.now())
                        .build()));

        StepVerifier.create(paymentService.processPayment(request))
                .expectError(NotEnoughMoneyException.class)
                .verify();

        verify(billingAccountRepository, times(1)).findFirstByOrderByCreatedAt();
    }

    @Test
    void whenDepositMoney_thenMoneyShouldBeAddedToAccount() {
        var request = new PaymentRequest();
        request.setValue(BigDecimal.valueOf(900));

        when(billingAccountRepository.findFirstByOrderByCreatedAt())
                .thenReturn(Mono.just(BillingAccount.builder()
                        .id(1L)
                        .money(BigDecimal.valueOf(1000))
                        .createdAt(LocalDate.now())
                        .modifiedAt(LocalDate.now())
                        .build()));

        when(billingAccountRepository.save(any(BillingAccount.class)))
                .thenReturn(Mono.just(BillingAccount.builder()
                        .id(1L)
                        .money(BigDecimal.valueOf(500))
                        .createdAt(LocalDate.now())
                        .modifiedAt(LocalDate.now())
                        .build()));

        StepVerifier.create(paymentService.processPayment(request))
                .expectNextMatches(balanceResponse -> balanceResponse.getValue() != null
                        && balanceResponse.getValue().equals(BigDecimal.valueOf(500)))
                .verifyComplete();

        verify(billingAccountRepository, times(1)).findFirstByOrderByCreatedAt();
        verify(billingAccountRepository, times(1)).save(any(BillingAccount.class));
    }
}