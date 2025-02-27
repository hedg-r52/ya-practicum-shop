package ru.yandex.practicum.shop.service;

import ru.yandex.practicum.shop.entity.Order;
import ru.yandex.practicum.shop.entity.OrderStatus;

import java.util.Optional;

public interface OrderService {
    Optional<Order> findLastActiveOrder();

    Optional<Order> findByIdAndStatus(Long id, OrderStatus status);
}
