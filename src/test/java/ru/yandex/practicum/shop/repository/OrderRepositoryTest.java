package ru.yandex.practicum.shop.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.shop.AbstractTestContainer;
import ru.yandex.practicum.shop.entity.Order;
import ru.yandex.practicum.shop.entity.OrderStatus;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class OrderRepositoryTest extends AbstractTestContainer {

    @Autowired
    OrderRepository orderRepository;

    @Test
    void whenFindFistByStatusOrderByCreatedAtDesc_thenShouldReturnOrder() {
        Order order = Order.builder()
                .status(OrderStatus.ACTIVE)
                .createdAt(LocalDate.now())
                .build();
        orderRepository.save(order).block();

        StepVerifier.create(orderRepository.findFirstByStatusOrderByCreatedAtDesc(OrderStatus.ACTIVE))
                .assertNext(found -> assertEquals(OrderStatus.ACTIVE, found.getStatus()))
                .verifyComplete();
    }

    @Test
    void whenFindByIdAndStatus_thenShouldReturnOrder() {
        Order order = Order.builder()
                .status(OrderStatus.ACTIVE)
                .createdAt(LocalDate.now())
                .build();
        orderRepository.save(order).block(); // Синхронное сохранение для упрощения примера

        Mono<Order> foundOrderMono = orderRepository.findByIdAndStatus(1L, OrderStatus.ACTIVE);

        StepVerifier.create(foundOrderMono)
                .assertNext(found -> {
                    assertEquals(OrderStatus.ACTIVE, found.getStatus());
                    assertEquals(1L, found.getId());
                })
                .verifyComplete();
    }
}
