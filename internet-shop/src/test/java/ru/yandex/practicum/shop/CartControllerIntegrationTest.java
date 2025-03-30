package ru.yandex.practicum.shop;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;
import ru.yandex.practicum.shop.entity.Order;
import ru.yandex.practicum.shop.entity.OrderStatus;
import ru.yandex.practicum.shop.entity.Product;
import ru.yandex.practicum.shop.repository.OrderRepository;
import ru.yandex.practicum.shop.repository.ProductRepository;
import ru.yandex.practicum.shop.service.CartService;
import ru.yandex.practicum.shop.service.OrderService;
import ru.yandex.practicum.shop.service.PaymentService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@AutoConfigureWebTestClient
@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("integration")
class CartControllerIntegrationTest extends AbstractTestContainer {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    CartService cartService;

    @Autowired
    OrderService orderService;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    PaymentService paymentService;

    @BeforeAll
    void setUp() {
        productRepository.deleteAll().block();
        orderRepository.deleteAll().block();

        List<Product> products = IntStream.rangeClosed(1, 5)
                .mapToObj(i -> Product.builder()
                        .name("Product 0" + i)
                        .description("Description of Product 0" + i)
                        .price(100.00f * i)
                        .build())
                .toList();

        productRepository.saveAll(products)
                .collectList()
                .doOnTerminate(() -> StepVerifier.create(productRepository.findAll())
                        .expectNextCount(5)
                        .verifyComplete())
                .block();

        Order order = Order.builder()
                .status(OrderStatus.ACTIVE)
                .createdAt(LocalDate.now())
                .build();

        orderRepository.save(order)
                .as(StepVerifier::create)
                .expectNextMatches(savedOrder -> savedOrder.getId() != null)
                .verifyComplete();

        assertDoesNotThrow(() -> {
            paymentService.depositPayment(BigDecimal.valueOf(5000))
                    .block();
        });
    }

    @Test
    void testGetCart() {
        webTestClient.get()
                .uri("/cart")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(response -> assertThat(response.getResponseBody()).contains("cart"));
    }

    @Test
    void testAddToCart() {
        webTestClient.post()
                .uri("/cart/add/2")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true);
    }

    @Test
    void testUpdateQuantity() {
        var productId = productRepository.findAll().blockFirst().getId();
        cartService.addProduct(productId).block();

        int change = 2;

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path("/cart/update/" + productId)
                        .queryParam("change", change)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true);
    }

    @Test
    void testRemoveFromCart() {
        var productId = 2L;

        cartService.addProduct(productId).block();

        webTestClient.post()
                .uri("/cart/remove/" + productId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true);

        // Проверяем, что продукт реально удалился
        webTestClient.get()
                .uri("/cart")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(response -> assertThat(response.getResponseBody()).doesNotContain("Product 02"));
    }

    @Test
    void testConfirmPurchase() {
        Order order = Order.builder()
                .status(OrderStatus.ACTIVE)
                .createdAt(LocalDate.now())
                .build();

        var savedOrder = orderRepository.save(order).block();

        webTestClient.post()
                .uri("/cart/purchase/" + savedOrder.getId())
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/order/summary/" + savedOrder.getId());

        // Проверяем, что заказ действительно подтвержден
        orderRepository.findById(savedOrder.getId())
                .as(StepVerifier::create)
                .expectNextMatches(updatedOrder -> updatedOrder.getStatus() == OrderStatus.PAID)
                .verifyComplete();
    }
}
