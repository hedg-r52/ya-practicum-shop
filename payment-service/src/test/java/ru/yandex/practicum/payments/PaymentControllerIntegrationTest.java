package ru.yandex.practicum.payments;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.yandex.practicum.payments.domain.DepositRequest;
import ru.yandex.practicum.payments.domain.PaymentRequest;
import ru.yandex.practicum.payments.model.BillingAccount;
import ru.yandex.practicum.payments.repository.BillingAccountRepository;
import ru.yandex.practicum.payments.service.PaymentService;

import java.math.BigDecimal;
import java.time.LocalDate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class PaymentControllerIntegrationTest extends AbstractTestContainer {
    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private BillingAccountRepository billingAccountRepository;

    @BeforeEach
    void setUp() {
        billingAccountRepository.deleteAll()
                .block();

        var billingAccount = BillingAccount.builder()
                .money(BigDecimal.valueOf(100))
                .createdAt(LocalDate.now())
                .modifiedAt(LocalDate.now())
                .build();

        billingAccountRepository.save(billingAccount).block();
    }

    @Test
    void whenGetBalance_thenShouldReturnBalance() {
        webTestClient.get()
                .uri("/payments")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.value").isEqualTo(100);
    }

    @Test
    void whenProcessPayments_thenShouldProcessPayment() {
        var request = new PaymentRequest();
        request.setValue(BigDecimal.valueOf(50));

        webTestClient.patch()
                .uri("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.value").isEqualTo(50);
    }

    @Test
    void whenPaymentsProcess_thenShouldReturnNotEnoughMoneyException() {
        var request = new PaymentRequest();
        request.setValue(BigDecimal.valueOf(2500));

        webTestClient.patch()
                .uri("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().is4xxClientError()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Недостаточно денег");
    }

    @Test
    void paymentsDepositPost_shouldAddMoneyToBalance() {
        var request = new DepositRequest();
        request.setValue(BigDecimal.valueOf(1500));

        webTestClient.post()
                .uri("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.value").isEqualTo(1600);
    }
}
