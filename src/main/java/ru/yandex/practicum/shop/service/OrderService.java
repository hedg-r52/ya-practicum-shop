package ru.yandex.practicum.shop.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.shop.dto.OrderDto;
import ru.yandex.practicum.shop.entity.Order;
import ru.yandex.practicum.shop.entity.OrderStatus;

public interface OrderService {
    Mono<Order> findLastActiveOrder();

    Mono<Order> findByIdAndStatus(Long id, OrderStatus status);

    Mono<Page<OrderDto>> findAll(Pageable pageable);
}
