package ru.yandex.practicum.shop.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.dto.OrderDto;
import ru.yandex.practicum.shop.dto.OrderItemDto;
import ru.yandex.practicum.shop.dto.ProductDto;
import ru.yandex.practicum.shop.entity.OrderStatus;
import ru.yandex.practicum.shop.service.OrderService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @MockitoBean
    OrderService orderService;

    @Test
    void whenGetOrders_shouldGetOrderList() {
        Pageable pageable = PageRequest.of(0, 2);

        var order1 = new OrderDto();
        order1.setId(1L);
        order1.setStatus(OrderStatus.ACTIVE);
        var order2 = new OrderDto();
        order2.setId(1L);
        order2.setStatus(OrderStatus.ACTIVE);

        var orders = List.of(order1, order2);

        Page<OrderDto> orderPage = new PageImpl<>(orders, pageable, orders.size());

        when(orderService.findAll(any(Pageable.class))).thenReturn(Mono.just(orderPage));

        webTestClient.get()
                .uri(builder ->
                        builder
                                .path("/order")
                                .queryParam("page", "1")
                                .queryParam("size", "2")
                                .build()
                )
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(response -> {
                    String body = response.getResponseBody();
                    assertNotNull(body);
                    assertTrue(body.contains("<div class=\"orders\">"));
                });

        verify(orderService, times(1))
                .findAll(PageRequest.of(0, 2, Sort.by("created_at").ascending().and(Sort.by("id").ascending())));
    }

    @Test
    void whenGetSummary_shouldShowSummaryOrderPage() {
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
        var orderDto = OrderDto.builder()
                .id(1L)
                .orderItems(new ArrayList<>(List.of(orderItem1)))
                .totalPrice(100.0f)
                .createdAt(LocalDate.now())
                .status(OrderStatus.PAID)
                .build();

        when(orderService.findByIdAndStatus(anyLong(), any(OrderStatus.class)))
                .thenReturn(Mono.just(orderDto));

        webTestClient.get()
                .uri("/order/summary/{id}", 1L)
                .exchange()
                .expectStatus().isOk();

        verify(orderService, times(1)).findByIdAndStatus(anyLong(), any(OrderStatus.class));
    }

}
