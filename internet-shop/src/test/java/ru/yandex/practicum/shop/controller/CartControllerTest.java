package ru.yandex.practicum.shop.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.config.TestCacheConfig;
import ru.yandex.practicum.shop.dto.OrderDto;
import ru.yandex.practicum.shop.dto.OrderItemDto;
import ru.yandex.practicum.shop.dto.ProductDto;
import ru.yandex.practicum.shop.entity.OrderStatus;
import ru.yandex.practicum.shop.service.CartService;
import ru.yandex.practicum.shop.service.OrderService;
import ru.yandex.practicum.shop.service.PaymentService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(CartController.class)
@Import(TestCacheConfig.class)
@ActiveProfiles("test")
class CartControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @MockitoBean
    PaymentService paymentService;

    @MockitoBean
    CartService cartService;

    @MockitoBean
    OrderService orderService;

    @Test
    void whenGetCart_shouldReturnListOfProducts() {
        OrderDto orderDto = prepareOrder(OrderStatus.ACTIVE);
        when(cartService.getCart())
                .thenReturn(Mono.just(orderDto));

        webTestClient.get()
                .uri("/cart")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .consumeWith(response -> {
                    String body = response.getResponseBody();
                    assertNotNull(body);
                    assertTrue(body.contains("<div class=\"cart-wrapper\">"));
                });

        verify(cartService, times(1)).getCart();
    }

    @Test
    void whenGetCheckoutPage_shouldReturnCheckoutOrder() {
        OrderDto orderDto = prepareOrder(OrderStatus.CHECKOUT);
        when(orderService.findByIdAndStatus(1L, OrderStatus.CHECKOUT))
                .thenReturn(Mono.just(orderDto));

        webTestClient.get()
                .uri("/cart/checkout/{id}", 1L)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .consumeWith(response -> {
                    String body = response.getResponseBody();
                    assertNotNull(body);
                    assertTrue(body.contains("<div class=\"checkout-wrapper\">"));
                });

        verify(orderService, times(1)).findByIdAndStatus(anyLong(), any(OrderStatus.class));
    }

    @Test
    void whenPostCheckout_shouldChangeOrderStatusToCheckout() {
        when(cartService.moveCartToCheckout(1L))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/cart/checkout/{id}", 1L)
                .exchange()
                .expectStatus().is3xxRedirection();

        verify(cartService, times(1)).moveCartToCheckout(1L);
    }

    @Test
    void whenPostPurchase_shouldChangeOrderStatusToPaid() {
        when(cartService.confirmPurchase(1L))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/cart/purchase/{id}", 1L)
                .exchange()
                .expectStatus().is3xxRedirection();

        verify(cartService, times(1)).confirmPurchase(1L);
    }


    @Test
    void whenPostAddToCart_shouldAddProductToCart() {
        when(cartService.addProduct(1L))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/cart/add/{id}", 1L)
                .exchange()
                .expectStatus().isOk();

        verify(cartService, times(1)).addProduct(anyLong());
    }

    @Test
    void whenPostUpdateQuantity_shouldChangeQuantity() {
        when(cartService.updateQuantity(1L, 3))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri(builder -> builder
                        .path("/cart/update/{id}")
                        .queryParam("change", "3")
                        .build(1L))
                .exchange()
                .expectStatus().isOk();

        verify(cartService, times(1)).updateQuantity(anyLong(), anyInt());
    }

    @Test
    void whenPostRemoveProductFromCart_shouldRemoveOrderItemFromCart() {
        when(cartService.removeProduct(1L))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/cart/remove/{id}", 1L)
                .exchange()
                .expectStatus()
                .isOk();

        verify(cartService, times(1)).removeProduct(1L);
    }

    private OrderDto prepareOrder(OrderStatus status) {
        var product1 = ProductDto.builder()
                .id(1L)
                .name("Product 01")
                .description("Description of Product 01")
                .price(20.0f)
                .build();
        var orderItem1 = OrderItemDto.builder()
                .id(1L)
                .orderId(1L)
                .productId(1L)
                .product(product1)
                .quantity(5)
                .build();
        return OrderDto.builder()
                .id(1L)
                .orderItems(new ArrayList<>(List.of(orderItem1)))
                .totalPrice(100.0f)
                .createdAt(LocalDate.now())
                .status(status)
                .build();
    }
}