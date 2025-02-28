package ru.yandex.practicum.shop.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.shop.dto.OrderDto;
import ru.yandex.practicum.shop.entity.Order;
import ru.yandex.practicum.shop.entity.OrderStatus;

import java.util.Optional;

public interface OrderService {
    Optional<Order> findLastActiveOrder();

    Optional<Order> findByIdAndStatus(Long id, OrderStatus status);

    Page<OrderDto> findAll(Pageable pageable);
}
